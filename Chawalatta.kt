package com.NatureNest.phoneauthkt

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.NestedScrollView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class Chawalatta : AppCompatActivity(), attacartiteam.CartControlsListener {
    private lateinit var dbCartRef: DatabaseReference
    private lateinit var dbHistoryRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var quantityEditText: TextView
    private lateinit var paisaEditText: TextView
    private lateinit var customerTypeMessage: EditText
    private lateinit var unitToggleButton: Button
    private lateinit var unitTextView: TextView
    private var lastSelectedRadioButton: RadioButton? = null


    // Declare radio buttons
    private lateinit var fineRadioButton: RadioButton
    private lateinit var mediumRadioButton: RadioButton
    private lateinit var coarseRadioButton: RadioButton

    private var scrollView: NestedScrollView? = null
    private var outerScrollView: ScrollView? = null
    private val scrollDelay = 50L
    private val scrollStep = 1
    private var isAutoScrolling = true
    private val scrollHandler = Handler(Looper.getMainLooper())

    private var isPcsMode = false // Default to KG mode for flour
    private val pricePerKg = 60 // ₹60 per kg
    private val hasShownMinQuantityToast = false
    private val quantityChangeInterval = 100L
    private val quantityHandler = Handler(Looper.getMainLooper())
    private var currentToast: Toast? = null
    private val notAvailableFruits = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge layout
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_chawalatta)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false

        // Initialize Firebase components
        dbCartRef = FirebaseDatabase.getInstance().getReference("cart")
        dbHistoryRef = FirebaseDatabase.getInstance().getReference("history")
        auth = FirebaseAuth.getInstance()



        // Initialize radio buttons
        fineRadioButton = findViewById(R.id.Fine)
        mediumRadioButton = findViewById(R.id.Normal)
        coarseRadioButton = findViewById(R.id.Coarse)

        // Set click listeners for radio buttons
        fineRadioButton.setOnClickListener { handleGrindingRadioButtonClick(fineRadioButton) }
        mediumRadioButton.setOnClickListener { handleGrindingRadioButtonClick(mediumRadioButton) }
        coarseRadioButton.setOnClickListener { handleGrindingRadioButtonClick(coarseRadioButton) }


        // Initialize views
        scrollView = findViewById(R.id.scrollView2)
        scrollView?.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    isAutoScrolling = false
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    isAutoScrolling = true
                    startAutoScroll()
                }
            }
            false
        }

        outerScrollView = findViewById(R.id.outerScrollView)
        ViewCompat.setOnApplyWindowInsetsListener(outerScrollView!!) { view, insets ->
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemInsets.top, 0, systemInsets.bottom)
            insets
        }

        // Initialize fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, attacartiteam.newInstance())
            .commit()

        // Setup auto-scrolling
        scrollView?.viewTreeObserver?.addOnGlobalLayoutListener {
            startAutoScroll()
        }

        // Fetch not available products
        fetchNotAvailableFruits()
    }

    private fun fetchNotAvailableFruits() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("not_available")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                notAvailableFruits.clear()
                for (child in snapshot.children) {
                    val fruitName = child.key?.lowercase()
                    fruitName?.let { notAvailableFruits.add(it) }
                }
                setupHorizontalFruitsAvailability()
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Failed to fetch availability data", true)
            }
        })
    }

    private fun setupHorizontalFruitsAvailability() {
        updateHorizontalFruitAvailability(findViewById(R.id.horizontal_strawberry_not_available), "strawberry")
        updateHorizontalFruitAvailability(findViewById(R.id.horizontal_banana_not_available), "banana")
        updateHorizontalFruitAvailability(findViewById(R.id.horizontal_grapes_not_available), "grapes")
        updateHorizontalFruitAvailability(findViewById(R.id.horizontal_kiwi_not_available), "kiwi")
        updateHorizontalFruitAvailability(findViewById(R.id.horizontal_caroot_not_available), "caroot")
        updateHorizontalFruitAvailability(findViewById(R.id.horizontal_garlic_not_available), "garlic")
        updateHorizontalFruitAvailability(findViewById(R.id.horizontal_tomato_not_available), "tomato")
        updateHorizontalFruitAvailability(findViewById(R.id.horizontal_potato_not_available), "potato")
        updateHorizontalFruitAvailability(findViewById(R.id.horizontal_onion_not_available), "onion")
        updateHorizontalFruitAvailability(findViewById(R.id.horizontal_multigrainbtn_not_available), "multigrainbtn")
        updateHorizontalFruitAvailability(findViewById(R.id.horizontal_gehuatta_not_available), "gehuatta")
        updateHorizontalFruitAvailability(findViewById(R.id.horizontal_mpsehorebtn_not_available), "mpsehorebtn")
        updateHorizontalFruitAvailability(findViewById(R.id.horizontal_chawalatta_not_available), "Bajraatta")
        updateHorizontalFruitAvailability(findViewById(R.id.horizontal_nachaniatta_not_available), "nachaniatta")
        updateHorizontalFruitAvailability(findViewById(R.id.horizontal_cowmilk1_not_available), "cowmilkbtn")
        updateHorizontalFruitAvailability(findViewById(R.id.horizontal_cowghee_not_available), "cowghee")
        updateHorizontalFruitAvailability(findViewById(R.id.horizontal_buffalomilk_not_available), "buffalomilk")
        updateHorizontalFruitAvailability(findViewById(R.id.horizontal_buffaloghee_not_available), "buffaloghee")
        updateHorizontalFruitAvailability(findViewById(R.id.horizontal_paneer_not_available), "paneer")

    }

    private fun updateHorizontalFruitAvailability(overlay: ImageView, fruitName: String) {
        overlay.visibility = if (notAvailableFruits.contains(fruitName.lowercase())) View.VISIBLE else View.GONE
    }

    fun openstrawberryActivity(view: View) {
        if (!notAvailableFruits.contains("apple")) startActivity(Intent(this, Strawberry::class.java))
    }

    fun openMangoActivity(view: View) {
        if (!notAvailableFruits.contains("mango")) startActivity(Intent(this, Mango::class.java))
    }

    fun openBananaActivity(view: View) {
        if (!notAvailableFruits.contains("banana")) startActivity(Intent(this, Banana::class.java))
    }

    fun openGrapesActivity(view: View) {
        if (!notAvailableFruits.contains("grapes")) startActivity(Intent(this, Grapes::class.java))
    }

    fun openKiwiActivity(view: View) {
        if (!notAvailableFruits.contains("kiwi")) startActivity(Intent(this, Kiwi::class.java))
    }
    fun openFruitsActivity(view: View) {

        startActivity(Intent(this, Fruits::class.java))
    }

    fun opentomatoActivity(view: View) {
        if (!notAvailableFruits.contains("tomato")) {
            startActivity(Intent(this, Tomato::class.java))
        }
    }
    fun openpotatoActivity(view: View) {
        if (!notAvailableFruits.contains("potato")) {
            startActivity(Intent(this, Potato::class.java))
        }
    }
    fun openonionActivity(view: View) {
        if (!notAvailableFruits.contains("onion")) {
            startActivity(Intent(this, Onion::class.java))
        }
    }
    fun opengarlicActivity(view: View) {
        if (!notAvailableFruits.contains("garlic")) {
            startActivity(Intent(this, Garlic::class.java))
        }
    }

    fun openCarootActivity(view: View) {
        if (!notAvailableFruits.contains("caroot")) {
            startActivity(Intent(this, Carrot::class.java))
        }
    }

    fun openmultigrain1Activity(view: View) {

        startActivity(Intent(this, Vegetables::class.java))
    }


    fun openmultigrainbtnActivity(view: View) {
        if (!notAvailableFruits.contains("multigrainbtn")) {
            startActivity(Intent(this, MultiGrainatta::class.java))
        }
    }
    fun opengehuattaActivity(view: View) {
        if (!notAvailableFruits.contains("gehuatta")) {
            startActivity(Intent(this, gahuatta::class.java))
        }
    }
    fun openmpsehorebtnActivity(view: View) {
        if (!notAvailableFruits.contains("mpsehorebtn")) {
            startActivity(Intent(this, MpSehoreatta::class.java))
        }
    }
    fun openchawalattaActivity(view: View) {
        if (!notAvailableFruits.contains("Bajraatta")) {
            startActivity(Intent(this, Bajraatta::class.java))
        }
    }
    fun opennachaniattaActivity(view: View) {
        if (!notAvailableFruits.contains("nachaniatta")) {
            startActivity(Intent(this, Nachaniatta::class.java))
        }
    }

    fun openattaActivity(view: View) {

        startActivity(Intent(this, viewmore::class.java))
    }

    fun opencowmilk1Activity(view: View) {
        if (!notAvailableFruits.contains("cowmilk1")) {
            startActivity(Intent(this, cowmilk::class.java))
        }
    }
    fun opencowgheeActivity(view: View) {
        if (!notAvailableFruits.contains("cowghee")) {
            startActivity(Intent(this, cowghee::class.java))
        }
    }
    fun openbuffalomilkActivity(view: View) {
        if (!notAvailableFruits.contains("buffalomilk")) {
            startActivity(Intent(this, buffalomilk::class.java))
        }
    }
    fun openbuffalogheeActivity(view: View) {
        if (!notAvailableFruits.contains("buffaloghee")) {
            startActivity(Intent(this, buffaloghee::class.java))
        }
    }
    fun openpaneerActivity(view: View) {
        if (!notAvailableFruits.contains("paneer")) {
            startActivity(Intent(this, paneer::class.java))
        }
    }

    fun opendairy1Activity(view: View) {

        startActivity(Intent(this, Dairy::class.java))
    }
    override fun onAddToCartClicked() {
        val added = addToCart()
        if (added) {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            val viewToCart = fragment?.view?.findViewById<ImageButton>(R.id.view_to_cart)
            viewToCart?.visibility = View.VISIBLE
        }
    }


    override fun onViewCartClicked() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("openCart", true)
        startActivity(intent)
        finish()
    }

    private fun startAutoScroll() {
        scrollHandler.removeCallbacksAndMessages(null)

        scrollHandler.post(object : Runnable {
            private var isScrollingDown = true

            override fun run() {
                if (!isAutoScrolling) return

                scrollView?.apply {
                    if (isScrollingDown) {
                        scrollBy(0, scrollStep)
                        if (scrollY >= getChildAt(0).measuredHeight - height) {
                            isScrollingDown = false
                        }
                    } else {
                        scrollBy(0, -scrollStep)
                        if (scrollY <= 0) {
                            isScrollingDown = true
                        }
                    }
                }
                scrollHandler.postDelayed(this, scrollDelay)
            }
        })
    }

    fun setupFragmentViews(fragment: attacartiteam) {
        quantityEditText = fragment.requireView().findViewById(R.id.editTextQuantity)
        paisaEditText = fragment.requireView().findViewById(R.id.paisa)
        unitTextView = fragment.requireView().findViewById(R.id.Kg)
        unitToggleButton = fragment.requireView().findViewById(R.id.unitToggleButton)
        customerTypeMessage = fragment.requireView().findViewById(R.id.phonenumber)

        // Set initial unit mode
        unitTextView.text = "KG"
        unitToggleButton.text = "KG"
        paisaEditText.text = "60"

        // Set click listeners for buttons in fragment
        val plusButton: Button = fragment.requireView().findViewById(R.id.plusebtn)
        val minusButton: Button = fragment.requireView().findViewById(R.id.minusbtn)

        // Short-click for increment and decrement
        plusButton.setOnClickListener { increaseQuantityAndPrice() }
        minusButton.setOnClickListener { decreaseQuantityAndPrice() }

        // Long-press handlers
        plusButton.setOnLongClickListener {
            quantityHandler.post(object : Runnable {
                override fun run() {
                    increaseQuantityAndPrice()
                    quantityHandler.postDelayed(this, quantityChangeInterval)
                }
            })
            true
        }

        minusButton.setOnLongClickListener {
            quantityHandler.post(object : Runnable {
                override fun run() {
                    decreaseQuantityAndPrice()
                    quantityHandler.postDelayed(this, quantityChangeInterval)
                }
            })
            true
        }

        plusButton.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP ||
                event.action == android.view.MotionEvent.ACTION_CANCEL) {
                quantityHandler.removeCallbacksAndMessages(null)
            }
            false
        }

        minusButton.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP ||
                event.action == android.view.MotionEvent.ACTION_CANCEL) {
                quantityHandler.removeCallbacksAndMessages(null)
            }
            false
        }

        // Unit toggle button - disabled for flour (only KG)
        unitToggleButton.setOnClickListener {
            // No action for flour as we only sell by KG
        }
    }

    private fun handleGrindingRadioButtonClick(clickedRadioButton: RadioButton) {
        // Uncheck all radio buttons first
        fineRadioButton.isChecked = false
        mediumRadioButton.isChecked = false
        coarseRadioButton.isChecked = false

        // Check the selected radio button
        clickedRadioButton.isChecked = true
        lastSelectedRadioButton = clickedRadioButton
    }

    private fun getGrindingType(): String {
        return when (lastSelectedRadioButton?.id) {
            R.id.Normal -> "Medium"
            R.id.Fine -> "Fine"
            R.id.Coarse -> "Coarse"
            else -> "Unknown"
        }
    }


    private fun increaseQuantityAndPrice() {
        val currentQuantity = quantityEditText.text.toString().toIntOrNull() ?: 0
        val updatedQuantity = currentQuantity + 1
        quantityEditText.text = updatedQuantity.toString()
        paisaEditText.text = (updatedQuantity * pricePerKg).toString()
    }

    private fun decreaseQuantityAndPrice() {
        val currentQuantity = quantityEditText.text.toString().toIntOrNull() ?: 0
        if (currentQuantity > 1) {
            val updatedQuantity = currentQuantity - 1
            quantityEditText.text = updatedQuantity.toString()
            paisaEditText.text = (updatedQuantity * pricePerKg).toString()
        } else {
            showToast("Cannot decrease below 1 kg", true)
        }
    }

    private fun addToCart(): Boolean {
        if (!quantityEditText.text.isNullOrEmpty() &&
            !paisaEditText.text.isNullOrEmpty() &&
            lastSelectedRadioButton != null
        ) {
            val itemName = "Chawal Atta"
            val quantity = quantityEditText.text.toString().toInt()
            val paisa = paisaEditText.text.toString().toInt()
            val message = customerTypeMessage.text.toString()
            val grindingType = getGrindingType()

            val currentUser = auth.currentUser
            val uid = currentUser?.uid

            if (uid != null) {
                val cartItemKey = "$itemName${System.currentTimeMillis()}"
                val cartItem = ItemListWithoutGrinding(
                    itemName,
                    quantity,
                    paisa,
                    System.currentTimeMillis(),
                    message,
                    grindingType
                )

                dbCartRef.child(uid).child(cartItemKey).setValue(cartItem)
                dbHistoryRef.child(uid).child(cartItemKey).setValue(cartItem)

                showToast("Chawal Atta added to cart! \uD83C\uDF5A", false)
                return true
            } else {
                showToast("Please log in", true)
            }
        } else {
            showToast("Please select type of grinding", true)
        }
        return false
    }


    private fun showToast(message: String, isError: Boolean = false) {
        currentToast?.cancel()

        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.toast_layout, findViewById(R.id.toast_text))

        val text = layout.findViewById<TextView>(R.id.toast_text)
        text.text = message

        if (isError) {
            layout.setBackgroundResource(R.drawable.rounded_background_error)
            text.setTextColor(0xFFFF0000.toInt())
        } else {
            layout.setBackgroundResource(R.drawable.rounded_background_success)
            text.setTextColor(0xFF00FF00.toInt())
        }

        currentToast = Toast(applicationContext).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        quantityHandler.removeCallbacksAndMessages(null)
        scrollHandler.removeCallbacksAndMessages(null)
    }
}