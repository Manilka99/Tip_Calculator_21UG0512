package com.example.tipcalculator

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.tipcalculator.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.ceil

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val themeTitleList = arrayOf("Light","Dark","Auto (System Default)")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Dark mode
        val changeThemeBtn = findViewById<Button>(R.id.changeThemeBtn)


        val sharedPreferenceManger = SharedPreferenceManger(this)
        var checkedTheme = sharedPreferenceManger.theme

        val themeDialog = MaterialAlertDialogBuilder(this)
            .setTitle("Theme")
            .setPositiveButton("Ok"){_,_ ->
                sharedPreferenceManger.theme = checkedTheme
                AppCompatDelegate.setDefaultNightMode(sharedPreferenceManger.themeFlag[checkedTheme])

            }
            .setSingleChoiceItems(themeTitleList,checkedTheme){_, which ->
                checkedTheme = which
            }
            .setCancelable(false)

        //dark mode
        changeThemeBtn.setOnClickListener{
            themeDialog.show()
        }

        // Set up the spinner with an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.currency_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.currencySpinner.adapter = adapter


        // Set up OnItemSelectedListener for the currency spinner
        binding.currencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                // Clear the cost of service EditText
                binding.costOfService.text.clear()
                // Recalculate the tip amount and total bill
                updateUI()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Do nothing
            }
        }


        }

        // Set default currency to USD
        binding.currencySpinner.setSelection(1) // Assuming USD is at position 1

        // Set up OnClickListener for the Calculate button
        binding.calculateButton.setOnClickListener { calculateTip() }

        // Set up OnCheckedChangeListener for the tip options radio group
        binding.tipOptions.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.option_custom) {
                // If the custom tip option is selected, clear the predefined options
                binding.optionTwentyPercent.isChecked = false
                binding.optionFifteenPercent.isChecked = false
                binding.optionTenPercent.isChecked = false
            }
            updateUI()
        }
    }

    private fun calculateTip() {
        val stringInTextField = binding.costOfService.text.toString()

        if (stringInTextField.isNotEmpty()) {
            val cost = stringInTextField.toDouble()

            // Get the selected tip percentage
            val tipPercentage = when {
                binding.optionTwentyPercent.isChecked -> 0.20
                binding.optionFifteenPercent.isChecked -> 0.15
                binding.optionTenPercent.isChecked -> 0.10
                else -> {
                    // If none of the predefined options is selected, use the custom tip percentage
                    val customTipPercentage = binding.customTipEditText.text.toString().toDoubleOrNull() ?: 0.0
                    customTipPercentage / 100.0
                }
            }

            var tip = tipPercentage * cost
            val roundUp = binding.roundUpSwitch.isChecked
            if (roundUp) {
                tip = ceil(tip)
            }

            // Calculate total bill (including tip)
            val totalBill = cost + tip

            // Get the number of people
            val numberOfPeople = binding.editTextNumberOfPeople.text.toString().toIntOrNull() ?: 1

            // Calculate amount each person should pay
            val amountPerPerson = totalBill / numberOfPeople

            // Get the selected currency from the spinner
            val selectedCurrency = binding.currencySpinner.selectedItem.toString()
            val currencyCode = when (selectedCurrency) {
                "Sri Lankan Rupee (LKR)" -> "LKR"
                "US Dollar (USD)" -> "USD"
                else -> "USD" // Default to USD if not recognized
            }

            // Format the tip amount, total bill, and amount per person with the selected currency
            val locale = Locale("en", currencyCode)
            val currency = Currency.getInstance(currencyCode)
            val currencyTip = NumberFormat.getCurrencyInstance(locale).apply {
                this.currency = currency  // Set the currency to the NumberFormat instance
            }.format(tip)

            val currencyTotalBill = NumberFormat.getCurrencyInstance(locale).apply {
                this.currency = currency  // Set the currency to the NumberFormat instance
            }.format(totalBill)

            val currencyAmountPerPerson = NumberFormat.getCurrencyInstance(locale).apply {
                this.currency = currency  // Set the currency to the NumberFormat instance
            }.format(amountPerPerson)

            // Display the formatted tip, total bill, and amount per person in the respective TextViews
            binding.tipResult.text = getString(R.string.tip_amount, currencyTip)
            binding.totalBill.text = getString(R.string.total_bill, currencyTotalBill)
            binding.amountPerPerson.text = getString(R.string.amount_per_person, currencyAmountPerPerson)
        } else {
            // Handle the case where the cost is not a valid number
            binding.tipResult.text = getString(R.string.invalid_cost)
            binding.totalBill.text = "" // Clear total bill TextView
            binding.amountPerPerson.text = "" // Clear amount per person TextView
        }
    }

    private fun updateUI() {
        // Update the UI when the currency or tip option is changed
        binding.customTipEditText.visibility = if (binding.optionCustom.isChecked) View.VISIBLE else View.GONE
        calculateTip()
    }
}
