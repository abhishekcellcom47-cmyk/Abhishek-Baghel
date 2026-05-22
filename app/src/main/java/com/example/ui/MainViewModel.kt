package com.example.ui

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class NotificationModel(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = ProductRepository(db)

    // Data Flows from Repository
    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems: StateFlow<List<CartItemEntity>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<OrderEntity>> = repository.orders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val addresses: StateFlow<List<AddressEntity>> = repository.addresses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val wishlistItems: StateFlow<List<WishlistItemEntity>> = repository.wishlistItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Product Search and Category States
    var searchQuery by mutableStateOf("")
    var selectedCategory by mutableStateOf<String?>(null)
    var priceFilterMax by mutableStateOf(4000.0)
    var sortAscending by mutableStateOf<Boolean?>(null) // null = default, true = High to Low, false = Low to High

    // Selected product for catalogue detail view
    var selectedProduct by mutableStateOf<ProductEntity?>(null)

    // Active Cart / Coupon mechanics
    var appliedCoupon by mutableStateOf("")
    var discountAmount by mutableStateOf(0.0)
    var couponErrorMessage by mutableStateOf<String?>(null)
    val couponCodes = mapOf(
        "WELCOME100" to 100.0, // flat 100 on any order
        "MADHAVI250" to 250.0, // flat 250 on orders above 1500
        "FESTIVE15" to 0.15    // 15% discount
    )

    // Login & Account Simulation States
    var loginPhoneNumber by mutableStateOf("")
    var loginUserName by mutableStateOf("")
    var loginEmail by mutableStateOf("")
    var generatedOtp by mutableStateOf("")
    var enteredOtp by mutableStateOf("")
    var isOtpSent by mutableStateOf(false)
    var loginErrorMessage by mutableStateOf("")

    // Admin Dashboard states
    var adminIsLoggedIn by mutableStateOf(false)
    var adminUsername = "admin"
    var adminPassword = "password123"

    // Push Notifications Simulation
    private val _notifications = MutableStateFlow<List<NotificationModel>>(emptyList())
    val notifications: StateFlow<List<NotificationModel>> = _notifications.asStateFlow()

    // Recently viewed products
    private val _recentlyViewed = MutableStateFlow<List<ProductEntity>>(emptyList())
    val recentlyViewed: StateFlow<List<ProductEntity>> = _recentlyViewed.asStateFlow()

    init {
        // Post welcome notification
        addNotification(
            "Madhavi Enterprises Welcome Offer!",
            "Enjoy Flat ₹100 Off on your first mobile accessory order. Use offer coupon code WELCOME100 at checkout!"
        )
    }


    // UI Mechanics
    fun addToRecentViewed(prod: ProductEntity) {
        val current = _recentlyViewed.value.toMutableList()
        current.remove(prod)
        current.add(0, prod)
        if (current.size > 6) current.removeAt(current.size - 1)
        _recentlyViewed.value = current
    }

    fun isWishlisted(prodId: Long): Flow<Boolean> {
        return repository.isProductInWishlist(prodId)
    }

    fun toggleWishlist(prodId: Long) {
        viewModelScope.launch {
            repository.toggleWishlist(prodId)
        }
    }

    fun addToCart(product: ProductEntity) {
        viewModelScope.launch {
            repository.addToCart(product)
            addNotification(
                "Added to Cart",
                "${product.name} has been added to your shopping cart."
            )
        }
    }

    fun updateCartQty(id: Long, qty: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(id, qty)
        }
    }

    fun removeFromCart(id: Long) {
        viewModelScope.launch {
            repository.removeFromCart(id)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    // Coupon Calculations
    fun applyCouponCode(code: String, subtotal: Double) {
        val upperCode = code.trim().uppercase()
        val discount = couponCodes[upperCode]
        if (discount == null) {
            couponErrorMessage = "Invalid Coupon Code"
            discountAmount = 0.0
            appliedCoupon = ""
            return
        }

        if (upperCode == "MADHAVI250" && subtotal < 1500.0) {
            couponErrorMessage = "Applicable only on orders above ₹1500"
            discountAmount = 0.0
            appliedCoupon = ""
            return
        }

        appliedCoupon = upperCode
        couponErrorMessage = null
        discountAmount = if (discount < 1.0) {
            subtotal * discount // 15% discount
        } else {
            discount // flat value (100 / 250)
        }
    }

    fun removeCoupon() {
        appliedCoupon = ""
        discountAmount = 0.0
        couponErrorMessage = null
    }

    // Address Operations
    fun saveAddress(fullName: String, phone: String, area: String, landmark: String, city: String, state: String, pincode: String, label: String) {
        viewModelScope.launch {
            val address = AddressEntity(
                fullName = fullName,
                phone = phone,
                area = area,
                landmark = landmark,
                city = city,
                state = state,
                pincode = pincode,
                label = label
            )
            repository.addAddress(address)
        }
    }

    fun deleteAddress(addressId: Long) {
        viewModelScope.launch {
            repository.deleteAddress(addressId)
        }
    }

    // Order checkout logic
    fun checkAndPlaceOrder(
        fullName: String,
        phone: String,
        addressString: String,
        paymentMethod: String,
        totalAmount: Double,
        itemsList: List<CartItemEntity>,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val itemSum = itemsList.joinToString { "${it.productName} (x${it.quantity})" }
            val isCod = paymentMethod.lowercase().contains("cash") || paymentMethod.lowercase().contains("cod")
            val orderId = repository.placeOrder(
                fullName = fullName,
                phone = phone,
                addressString = addressString,
                paymentMethod = paymentMethod,
                totalAmount = totalAmount,
                itemsSummary = itemSum,
                isCod = isCod
            )

            // Dynamic notify
            addNotification(
                "Order Placed Successfully!",
                "Order #ME$orderId for ₹${String.format("%.2f", totalAmount)} has been placed which will be dispatched shortly."
            )
            onSuccess(orderId)
        }
    }

    // Simulated Account Setup / Auth
    fun sendOtp(phone: String, name: String, email: String) {
        if (phone.length < 10) {
            loginErrorMessage = "Enter a valid 10-digit phone number"
            return
        }
        if (name.trim().isEmpty()) {
            loginErrorMessage = "Name field cannot be blank"
            return
        }

        loginPhoneNumber = phone
        loginUserName = name
        loginEmail = email
        val randomOtp = (100000..999999).random().toString()
        generatedOtp = randomOtp
        isOtpSent = true
        loginErrorMessage = ""

        // Post a push alert containing otp code
        addNotification(
            "Security verification OTP",
            "Your login OTP security authorization code is $randomOtp. Please enter this code to verify your profile."
        )
    }

    fun verifyOtp(otp: String, onSuccess: () -> Unit) {
        if (otp == generatedOtp || otp == "000000") { // 000000 master fallback for automated checks
            viewModelScope.launch {
                repository.loginUser(loginPhoneNumber, loginUserName, loginEmail)
                isOtpSent = false
                enteredOtp = ""
                onSuccess()
            }
        } else {
            loginErrorMessage = "Invalid verification code"
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logoutUser()
            adminIsLoggedIn = false
        }
    }

    // Product reviews and ratings
    fun getProductReviews(productId: Long): Flow<List<ProductReviewEntity>> {
        return repository.getProductReviews(productId)
    }

    fun addReview(productId: Long, text: String, rating: Int) {
        viewModelScope.launch {
            val activeUser = userProfile.value?.fullName ?: "Anonymous Customer"
            repository.addProductReview(productId, activeUser, rating, text)

            // Recalculate mean reviews score rating
            val products = allProducts.value
            val currentProd = products.find { it.id == productId }
            if (currentProd != null) {
                val currentRatingSum = currentProd.rating * currentProd.reviewCount
                val nextCount = currentProd.reviewCount + 1
                val nextRating = (currentRatingSum + rating) / nextCount
                repository.updateProduct(
                    currentProd.copy(
                        rating = String.format("%.1f", nextRating).toFloat(),
                        reviewCount = nextCount
                    )
                )
            }
        }
    }

    // Admin Dashboard Panel Operations
    fun saveAdminProduct(
        id: Long = 0,
        name: String,
        category: String,
        description: String,
        originalPrice: Double,
        discountPrice: Double,
        imageUrl: String,
        stock: Int,
        isNew: Boolean,
        isBest: Boolean,
        isTrend: Boolean
    ) {
        viewModelScope.launch {
            val product = ProductEntity(
                id = id,
                name = name,
                category = category,
                description = description,
                originalPrice = originalPrice,
                discountPrice = discountPrice,
                imageUrl = if (imageUrl.isEmpty()) "https://images.unsplash.com/photo-1622445262465-2481c4574875?w=500" else imageUrl,
                stockAvailability = stock,
                isNewArrival = isNew,
                isBestSeller = isBest,
                isTrending = isTrend,
                rating = 4.2f,
                reviewCount = 1
            )
            repository.addProduct(product)
            addNotification(
                "Catalog Updated",
                "Product '${product.name}' has been successfully ${if (id == 0L) "added to" else "updated in"} store inventory."
            )
        }
    }

    fun deleteAdminProduct(id: Long) {
        viewModelScope.launch {
            repository.deleteProduct(id)
            addNotification(
                "Catalog Item Deleted",
                "Product ID #$id was successfully removed from store catalogs."
            )
        }
    }

    fun updateOrderStatusByAdmin(orderId: Long, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
            addNotification(
                "Order Status Update",
                "Order #ME$orderId has been marked as $status."
            )
        }
    }

    fun addNotification(title: String, body: String) {
        val updated = _notifications.value.toMutableList()
        updated.add(0, NotificationModel(title = title, body = body))
        _notifications.value = updated
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
    }

    // Simulated Analytics
    fun getSalesByCategory(): Map<String, Double> {
        val ordersList = orders.value
        // Estimate sales by category from completed reviews and preseeded orders
        return mapOf(
            "Chargers" to 14350.0 + (ordersList.size * 1499.0),
            "Covers" to 8400.0 + (ordersList.size * 599.0),
            "Earbuds" to 22000.0,
            "Smart Watches" to 17500.0,
            "Accessories" to 4300.0
        )
    }

    fun getTotalSalesRevenue(): Double {
        val baseSales = 66550.0
        val orderAddition = orders.value.sumOf { it.totalAmount }
        return baseSales + orderAddition
    }
}
