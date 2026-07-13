package com.NatureNest.phoneauthkt

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class viewmore : AppCompatActivity() {
    private lateinit var chatbtn: ImageButton
    private lateinit var jawarbtn: ImageButton
    private lateinit var gahubtn: ImageButton
    private lateinit var bajrabtn: ImageButton
    private lateinit var chawalbtn: ImageButton
    private lateinit var nachanibtn: ImageButton
    private lateinit var multigrainbtn: ImageButton
    private lateinit var mpsehorebtn: ImageButton

    private lateinit var searchBar: EditText
    private lateinit var scrollView: ScrollView

    private val notAvailableFruits = mutableSetOf<String>()

    private val hints = arrayOf("Search for Jowar Atta", "Search for Nachani Atta", "Search for Bajara Atta")
    private var hintIndex = 0

    private lateinit var handler: Handler
    private lateinit var hintRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge layout
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_viewmore)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        val controller = WindowInsetsControllerCompat(window, window.decorView)
// Set white icons (false) or black icons (true) depending on background
        controller.isAppearanceLightStatusBars = false  // false = white icons
        controller.isAppearanceLightNavigationBars = false // false = white icons
        // Initialize ImageButtons
        chatbtn = findViewById(R.id.chatbtn)
        gahubtn = findViewById(R.id.gahubtn)
        jawarbtn = findViewById(R.id.jawarbtn)
        bajrabtn = findViewById(R.id.bajrabtn)
        chawalbtn = findViewById(R.id.chawalbtn)
        nachanibtn = findViewById(R.id.nachanibtn)
        multigrainbtn = findViewById(R.id.multigrainbtn)
        mpsehorebtn = findViewById(R.id.mpsehorebtn)

        // Initialize search bar and scrollView
        searchBar = findViewById(R.id.search_bar)
        scrollView = findViewById(R.id.margintop)
        val outerLayout: View = findViewById(R.id.atta)
        ViewCompat.setOnApplyWindowInsetsListener(outerLayout) { view, insets ->
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemInsets.top, 0, systemInsets.bottom)
            insets
        }

        // Fetch data for unavailable fruits
        fetchNotAvailableFruits()
        // Initialize the handler and set up the hint cycling functionality
        handler = Handler(Looper.getMainLooper())
        setupHintRunnable()

        // Start the hint cycling
        handler.post(hintRunnable)

        // Set click listeners for the buttons
        chatbtn.setOnClickListener { openActivity(Chat::class.java) }
        gahubtn.setOnClickListener { openActivity(gahuatta::class.java) }
        jawarbtn.setOnClickListener { openActivity(jawaratta::class.java) }
        bajrabtn.setOnClickListener { openActivity(Bajraatta::class.java) }
        chawalbtn.setOnClickListener { openActivity(Chawalatta::class.java) }
        nachanibtn.setOnClickListener { openActivity(Nachaniatta::class.java) }
        multigrainbtn.setOnClickListener { openActivity(MultiGrainatta::class.java) }
        mpsehorebtn.setOnClickListener { openActivity(MpSehoreatta::class.java) }

        // Set up search functionality with TextWatcher
        setupSearchFunctionality()
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
                updateUI()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@viewmore, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUI() {
        updateFruitButtonState(multigrainbtn, findViewById(R.id.multigrainbtn_not_available_image), "multigrainbtn")
        updateFruitButtonState(gahubtn, findViewById(R.id.gahubtn_not_available_image), "gahubtn")
        updateFruitButtonState(mpsehorebtn, findViewById(R.id.mpsehorebtn_not_available_image), "mpsehorebtn")
        updateFruitButtonState(jawarbtn, findViewById(R.id.jawarbtn_not_available_image), "jawarbtn")
        updateFruitButtonState(chawalbtn, findViewById(R.id.chawalbtn_not_available_image), "chawalbtn")
        updateFruitButtonState(nachanibtn, findViewById(R.id.nachanibtn_not_available_image), "nachanibtn")
        updateFruitButtonState(bajrabtn, findViewById(R.id.bajrabtn_not_available_image), "bajrabtn")

    }

    private fun updateFruitButtonState(button: ImageButton, overlay: ImageView, fruitName: String) {
        if (notAvailableFruits.contains(fruitName.lowercase())) {
            overlay.visibility = View.VISIBLE
            button.isEnabled = false
        } else {
            overlay.visibility = View.GONE
            button.isEnabled = true
        }
    }

    private fun setupSearchFunctionality() {
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()

                when {
                    query.contains("jawar") || query.startsWith("j") -> scrollToFragment(R.id.jjawarbtn)
                    query.contains("nachani") || query.startsWith("n") -> scrollToFragment(R.id.nnachanibtn)
                    query.contains("bajara") || query.startsWith("b") -> scrollToFragment(R.id.bbajrabtn)
                    query.contains("gahu") || query.startsWith("g") -> scrollToFragment(R.id.ggahubtn)
                    query.contains("multigrain") || query.startsWith("m") -> scrollToFragment(R.id.mmultigrainbtn)
                    query.contains("sehore") || query.startsWith("s") -> scrollToFragment(R.id.mmpsehorebtn)
                    query.contains("chawal") || query.startsWith("c") -> scrollToFragment(R.id.cchawalbtn)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun scrollToFragment(fragmentId: Int) {
        // Get the fragment's view by its ID
        val fragmentView = findViewById<View>(fragmentId)

        // Get the Y position of the fragment
        val yPos = fragmentView.top

        // Scroll the ScrollView to the fragment position
        scrollView.post {
            scrollView.smoothScrollTo(0, yPos)
        }
    }

    private fun setupHintRunnable() {
        hintRunnable = object : Runnable {
            override fun run() {
                searchBar.hint = hints[hintIndex]
                hintIndex = (hintIndex + 1) % hints.size
                handler.postDelayed(this, 3000)  // Change hint every 3 seconds
            }
        }
    }

    // Function to open a specific activity based on the provided class
    private fun openActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }

    // Clean up the handler to avoid memory leaks
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(hintRunnable)
    }
}
