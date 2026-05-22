package com.example.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductRepository(private val db: AppDatabase) {

    private val productDao = db.productDao()
    private val cartDao = db.cartDao()
    private val orderDao = db.orderDao()
    private val addressDao = db.addressDao()
    private val userProfileDao = db.userProfileDao()
    private val wishlistDao = db.wishlistDao()
    private val reviewDao = db.reviewDao()

    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val cartItems: Flow<List<CartItemEntity>> = cartDao.getCartItems()
    val orders: Flow<List<OrderEntity>> = orderDao.getAllOrders()
    val addresses: Flow<List<AddressEntity>> = addressDao.getAllAddresses()
    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getUserProfile()
    val wishlistItems: Flow<List<WishlistItemEntity>> = wishlistDao.getWishlistItems()

    init {
        // Pre-populate database with elegant mock data on startup if empty
        CoroutineScope(Dispatchers.IO).launch {
            if (productDao.getProductCount() == 0) {
                prepopulateDatabase()
            }
        }
    }

    private suspend fun prepopulateDatabase() = withContext(Dispatchers.IO) {
        val defaultProducts = listOf(
            ProductEntity(
                name = "65W Warp GaN Charger with Type-C Cable",
                category = "Chargers",
                description = "Ultra high speed Gallium Nitride Charger. Supports Dash, Warp, and PD protocols super-fast charging for Android terminals. Dual output port design.",
                originalPrice = 2499.0,
                discountPrice = 1499.0,
                imageUrl = "https://images.unsplash.com/photo-1622445262465-2481c4574875?w=500",
                stockAvailability = 25,
                isBestSeller = true,
                rating = 4.8f,
                reviewCount = 28
            ),
            ProductEntity(
                name = "Heavy Duty Type-C to Type-C 100W Braided Cable",
                category = "Data Cables",
                description = "Robust braided nylon with heavy-duty aluminum end plugs. Equipped with a real-time LCD wattage display checker tracker.",
                originalPrice = 799.0,
                discountPrice = 399.0,
                imageUrl = "https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=500",
                stockAvailability = 40,
                isTrending = true,
                rating = 4.6f,
                reviewCount = 14
            ),
            ProductEntity(
                name = "Studio Ultra Bass Earphones (3.5mm Jack)",
                category = "Earphones",
                description = "High fidelity dynamic 12mm drivers with inline crystal-clear microphone and volume control toggle. Braided tangle-free chords.",
                originalPrice = 899.0,
                discountPrice = 449.0,
                imageUrl = "https://images.unsplash.com/photo-1618384887929-16ec33fab9ef?w=500",
                stockAvailability = 35,
                isNewArrival = true,
                rating = 4.3f,
                reviewCount = 19
            ),
            ProductEntity(
                name = "X-Active True Wireless ANC Earbuds",
                category = "Earbuds",
                description = "Active Noise Cancellation up to 35dB, 40-hour combined playback with wireless charge case. IPX5 water dynamic seals.",
                originalPrice = 3999.0,
                discountPrice = 2199.0,
                imageUrl = "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=500",
                stockAvailability = 12,
                isBestSeller = true,
                rating = 4.7f,
                reviewCount = 52
            ),
            ProductEntity(
                name = "Magnetic MagSafe Matte Back Case Shield",
                category = "Covers",
                description = "Premium translucent back panel with impact-resistant corner airbags. Super strong neodymium magnet rings for charging integration.",
                originalPrice = 1199.0,
                discountPrice = 599.0,
                imageUrl = "https://images.unsplash.com/photo-1603302576837-37561b2e2302?w=500",
                stockAvailability = 50,
                isBestSeller = true,
                rating = 4.5f,
                reviewCount = 37
            ),
            ProductEntity(
                name = "11D Curved Edge-to-Edge Tempered Glass Screen Guard",
                category = "Tempered Glass",
                description = "Japanese Asahi 9H tempered toughness. Full screen edge wrap with extreme oleophobic smudge-proof coating, scratch defense.",
                originalPrice = 499.0,
                discountPrice = 249.0,
                imageUrl = "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=500",
                stockAvailability = 100,
                isBestSeller = false,
                rating = 4.4f,
                reviewCount = 8
            ),
            ProductEntity(
                name = "Original Replacement IPS Touch Screen Spare For Handsets",
                category = "Spare Parts",
                description = "OEM certified replacement digitizer assembly. High luminance, precise haptics, original color temperature presets. Professional kit.",
                originalPrice = 3499.0,
                discountPrice = 1899.0,
                imageUrl = "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=500",
                stockAvailability = 8,
                isNewArrival = true,
                rating = 4.2f,
                reviewCount = 4
            ),
            ProductEntity(
                name = "Cosmic Active Sport GPS Smart Watch",
                category = "Smart Watches",
                description = "Full 1.8\" AMOLED dial tracker. Precision independent GPS tracker, SpO2 sensor, active stress and pulse diagnostics with sleek notifications.",
                originalPrice = 6999.0,
                discountPrice = 3499.0,
                imageUrl = "https://images.unsplash.com/photo-1542496658-e33a6d0d50f6?w=500",
                stockAvailability = 15,
                isTrending = true,
                rating = 4.8f,
                reviewCount = 41
            ),
            ProductEntity(
                name = "ThunderBoom 30W RGB Wireless Bluetooth Speaker",
                category = "Bluetooth Speakers",
                description = "Interactive soundwave RGB concentric visual rings, deep dual-core subwoofers with custom bass reflex. Heavy battery back up.",
                originalPrice = 4999.0,
                discountPrice = 2499.0,
                imageUrl = "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=500",
                stockAvailability = 18,
                isTrending = true,
                rating = 4.7f,
                reviewCount = 23
            ),
            ProductEntity(
                name = "Universal Auto-Clamp Car Vent Phone Mount",
                category = "Accessories",
                description = "Gravity interlocking grip tabs, 360-degree rotating joint. Premium soft silicone claws prevent air-vent grille blemishes.",
                originalPrice = 899.0,
                discountPrice = 349.0,
                imageUrl = "https://images.unsplash.com/photo-1609592424109-dd9892f1b17c?w=500",
                stockAvailability = 30,
                rating = 4.1f,
                reviewCount = 11
            ),
            ProductEntity(
                name = "HyperCharge 20000mAh Power Bank (22.5W Fast)",
                category = "Accessories",
                description = "Multiple output ports including PD Type-C and QC USB ports. High density polymer cell with dynamic percentage digital display.",
                originalPrice = 2999.0,
                discountPrice = 1699.0,
                imageUrl = "https://images.unsplash.com/photo-1609592424109-dd9892f1b17c?w=500",
                stockAvailability = 22,
                isBestSeller = true,
                rating = 4.6f,
                reviewCount = 31
            ),
            ProductEntity(
                name = "Sports Pro Wireless Magnetic Neckband",
                category = "Earphones",
                description = "High density soft skin neckband with magnetic buds. Dual equalizer, premium noise isolating microphones, 30-hour backup runtime.",
                originalPrice = 1899.0,
                discountPrice = 899.0,
                imageUrl = "https://images.unsplash.com/photo-1618384887929-16ec33fab9ef?w=500",
                stockAvailability = 28,
                isNewArrival = false,
                rating = 4.4f,
                reviewCount = 15
            ),
            ProductEntity(
                name = "Handset Hardware Repair Precision Magnetic Driver Kit (48-in-1)",
                category = "Spare Parts",
                description = "Professional structural kit. Includes durable vanadium alloy tips, opening tools, anti-static micro-spudgers, and handy organizer cases.",
                originalPrice = 1499.0,
                discountPrice = 749.0,
                imageUrl = "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=500",
                stockAvailability = 10,
                isNewArrival = true,
                rating = 4.5f,
                reviewCount = 6
            )
        )
        productDao.insertProducts(defaultProducts)

        // Seed some reviews
        val demoReviews = listOf(
            ProductReviewEntity(productId = 1, userName = "Rahul Sharma", userRating = 5, comment = "Excellent charger! Charges my OP phone from 0 to 100% in under 45 minutes."),
            ProductReviewEntity(productId = 1, userName = "Amit Patel", userRating = 4, comment = "Build quality is stellar. Keeps cool during fast cycles though cord length is a bit short."),
            ProductReviewEntity(productId = 4, userName = "Priya Rao", userRating = 5, comment = "ANC is amazing at this price! Bass is punchy and deep. Highly recommended!"),
            ProductReviewEntity(productId = 8, userName = "Abhishek B.", userRating = 5, comment = "Extremely bright display. Accuracy of step tracking is spot on compared to Garmin.")
        )
        demoReviews.forEach { reviewDao.insertReview(it) }

        // Seed default addresses
        addressDao.insertAddress(
            AddressEntity(
                fullName = "Ramesh Kumar Enterprises",
                phone = "9876543210",
                area = "102, Madhavi Arcade, Sector-4",
                landmark = "Near Galaxy Cinema",
                city = "Mumbai",
                state = "Maharashtra",
                pincode = "400011",
                label = "Home"
            )
        )
    }

    // Product Admin operations
    suspend fun addProduct(product: ProductEntity): Long = withContext(Dispatchers.IO) {
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(id: Long) = withContext(Dispatchers.IO) {
        productDao.deleteProductById(id)
    }

    // Wishlist operations
    suspend fun toggleWishlist(productId: Long) = withContext(Dispatchers.IO) {
        val isFav = wishlistDao.isProductInWishlist(productId).first()
        if (isFav) {
            wishlistDao.deleteWishlist(productId)
        } else {
            wishlistDao.insertWishlist(WishlistItemEntity(productId))
        }
    }

    fun isProductInWishlist(productId: Long): Flow<Boolean> {
        return wishlistDao.isProductInWishlist(productId)
    }

    // Cart operations
    suspend fun addToCart(product: ProductEntity) = withContext(Dispatchers.IO) {
        val items = cartDao.getCartItems().first()
        val existing = items.find { it.id == product.id }
        if (existing != null) {
            cartDao.updateCartItemQuantity(product.id, existing.quantity + 1)
        } else {
            cartDao.insertCartItem(
                CartItemEntity(
                    id = product.id,
                    productName = product.name,
                    originalPrice = product.originalPrice,
                    discountPrice = product.discountPrice,
                    imageUrl = product.imageUrl,
                    category = product.category,
                    quantity = 1
                )
            )
        }
    }

    suspend fun updateCartQuantity(id: Long, qty: Int) = withContext(Dispatchers.IO) {
        if (qty <= 0) {
            cartDao.deleteCartItem(id)
        } else {
            cartDao.updateCartItemQuantity(id, qty)
        }
    }

    suspend fun removeFromCart(id: Long) = withContext(Dispatchers.IO) {
        cartDao.deleteCartItem(id)
    }

    suspend fun clearCart() = withContext(Dispatchers.IO) {
        cartDao.clearCart()
    }

    // Orders operations
    suspend fun placeOrder(
        fullName: String,
        phone: String,
        addressString: String,
        paymentMethod: String,
        totalAmount: Double,
        itemsSummary: String,
        isCod: Boolean
    ): Long = withContext(Dispatchers.IO) {
        val order = OrderEntity(
            addressFullName = fullName,
            addressPhone = phone,
            addressLine = addressString,
            paymentMethod = paymentMethod,
            totalAmount = totalAmount,
            itemsSummary = itemsSummary,
            isCod = isCod,
            status = "Confirmed" // Automatically system confirmation
        )
        val orderId = orderDao.insertOrder(order)
        cartDao.clearCart()
        orderId
    }

    suspend fun updateOrderStatus(orderId: Long, status: String) = withContext(Dispatchers.IO) {
        orderDao.updateOrderStatus(orderId, status)
    }

    // Address operations
    suspend fun addAddress(address: AddressEntity) = withContext(Dispatchers.IO) {
        addressDao.insertAddress(address)
    }

    suspend fun deleteAddress(addressId: Long) = withContext(Dispatchers.IO) {
        addressDao.deleteAddress(addressId)
    }

    // Reviews operations
    fun getProductReviews(productId: Long): Flow<List<ProductReviewEntity>> {
        return reviewDao.getReviewsForProduct(productId)
    }

    suspend fun addProductReview(productId: Long, userName: String, rating: Int, comment: String) = withContext(Dispatchers.IO) {
        val review = ProductReviewEntity(
            productId = productId,
            userName = userName,
            userRating = rating,
            comment = comment
        )
        reviewDao.insertReview(review)
    }

    // Local profile simulation
    suspend fun loginUser(phone: String, fullName: String, email: String) = withContext(Dispatchers.IO) {
        userProfileDao.clearProfile()
        userProfileDao.insertUserProfile(
            UserProfileEntity(
                phone = phone,
                fullName = fullName,
                email = email
            )
        )
    }

    suspend fun logoutUser() = withContext(Dispatchers.IO) {
        userProfileDao.clearProfile()
    }
}
