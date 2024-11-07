package com.example.lab3kotlin1
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val avgPowerInput = findViewById<EditText>(R.id.avg_power)
        val sigma1Input = findViewById<EditText>(R.id.sigma1)
        val sigma2Input = findViewById<EditText>(R.id.sigma2)
        val electricityPriceInput = findViewById<EditText>(R.id.electricity_price)
        val calculateButton = findViewById<Button>(R.id.calculate_button)
        val resultText = findViewById<TextView>(R.id.result_text)

        calculateButton.setOnClickListener {
            val avgPower = avgPowerInput.text.toString().toDoubleOrNull() ?: 0.0
            val sigma1 = sigma1Input.text.toString().toDoubleOrNull() ?: 0.0
            val sigma2 = sigma2Input.text.toString().toDoubleOrNull() ?: 0.0
            val electricityPrice = electricityPriceInput.text.toString().toDoubleOrNull() ?: 0.0


            val results = calculate(avgPower, sigma1, sigma2, electricityPrice)
            resultText.text = """
            ΔW1: ${String.format("%.2f", results.deltaW1)}
            ΔW2: ${String.format("%.2f", results.deltaW2)}
            Прибуток 1: ${String.format("%.1f", results.inc1)} грн
            Штраф 1: ${String.format("%.1f", results.f1)} грн
            Прибуток 2: ${String.format("%.1f", results.inc2)} грн
            Штраф 2: ${String.format("%.1f", results.f2)} грн
            Прибуток: ${String.format("%.1f", results.totalProfit)} тис. грн
        """.trimIndent()
        }
    }

    data class results(
        val deltaW1: Double,
        val deltaW2: Double,
        val inc1: Double,
        val f1: Double,
        val inc2: Double,
        val f2: Double,
        val totalProfit: Double
    )

    private fun calculate(avgPower: Double, sigma1: Double, sigma2: Double, elPrice: Double): results {
        val deltaW1 = integrate(
            { x, pc, sigma1 -> calculatePdW1(x, pc, sigma1) },
            4.75, 5.25, 1000, avgPower, sigma1
        )
        val W1 = avgPower * 24 * deltaW1
        val inc1 = W1 * elPrice
        val W2 = avgPower * 24 * (1 - deltaW1)
        val f1 = W2 * elPrice
        val deltaW2 = integrate(
            { x, pc, sigma2 -> calculatePdW2(x, pc, sigma2) },
            4.75, 5.25, 1000, avgPower, sigma2
        )
        val W3 = avgPower * 24 * deltaW2
        val inc2 = W3 * elPrice
        val W4 = avgPower * 24 * (1 - deltaW2)
        val f2 = W4 * elPrice
        val totalProfit = inc2 - f2

        return results(deltaW1, deltaW2, inc1, f1, inc2, f2, totalProfit)
    }

    // Функція інтегрування
    private fun integrate(
        func: (Double, Double, Double) -> Double,
        start: Double,
        end: Double,
        steps: Int,
        Pc: Double,
        sigma: Double
    ): Double {
        val step = (end - start) / steps
        var sum = 0.5 * (func(start, Pc, sigma) + func(end, Pc, sigma))

        var x = start + step
        while (x < end) {
            sum += func(x, Pc, sigma)
            x += step
        }

        return sum * step
    }

    // Функція для розрахунку нормального розподілу
    private fun calculatePdW1(p: Double, Pc: Double, sigma1: Double): Double {
        return (1 / (sigma1 * sqrt(2 * PI))) * exp(-(p - Pc).pow(2) / (2 * sigma1.pow(2)))
    }
    // Функція для розрахунку нормального розподілу
    private fun calculatePdW2(p: Double, Pc: Double, sigma2: Double): Double {
        return (1 / (sigma2 * sqrt(2 * PI))) * exp(-(p - Pc).pow(2) / (2 * sigma2.pow(2)))
    }
}

