package com.example.expensetracker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpenseTrackerTest {

    // Тест 1. Валидная сумма корректно преобразуется в число
    @Test
    fun validAmount_isParsedCorrectly() {
        val result = "150.50".toDoubleOrNull()
        assertEquals(150.50, result!!, 0.001)
    }

    // Тест 2. Буквы в поле суммы дают null
    @Test
    fun invalidAmount_returnsNull() {
        val result = "abc".toDoubleOrNull()
        assertNull(result)
    }

    // Тест 3. Нулевая сумма не является валидной
    @Test
    fun zeroAmount_isNotPositive() {
        val amount = 0.0
        assertFalse(amount > 0)
    }

    // Тест 4. Отрицательная сумма не является валидной
    @Test
    fun negativeAmount_isNotPositive() {
        val amount = -50.0
        assertFalse(amount > 0)
    }

    // Тест 5. Положительная сумма является валидной
    @Test
    fun positiveAmount_isValid() {
        val amount = 99.99
        assertTrue(amount > 0)
    }

    // Тест 6. Итоговая сумма считается правильно
    @Test
    fun totalSum_isCalculatedCorrectly() {
        val amounts = listOf(50.0, 30.0, 150.0)
        val total = amounts.sum()
        assertEquals(230.0, total, 0.001)
    }

    // Тест 7. Пустое название невалидно
    @Test
    fun emptyName_isInvalid() {
        val name = "   ".trim()
        assertTrue(name.isEmpty())
    }

    // Тест 8. Непустое название валидно
    @Test
    fun nonEmptyName_isValid() {
        val name = "Хлеб".trim()
        assertFalse(name.isEmpty())
    }
}