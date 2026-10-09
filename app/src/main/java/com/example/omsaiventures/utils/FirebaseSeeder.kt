package com.example.omsaiventures.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseSeeder {
    
    fun seedData() {
        val db = FirebaseFirestore.getInstance()
        
        // 1. Seed Company Data
        val companyData = hashMapOf(
            "name" to "OMSAI VENTURES",
            "sub" to "Registered Office: First Floor, 65, Shreya Park, Near Ravi Nagar, Gokul Road,\nHubli – 580030",
            "contact" to "Email: omsaiventures9@gmail.com\nPhone: 8073874620 / 9739316724",
            "gst" to "GSTIN: 29AAKF01084E1Z6"
        )
        
        db.collection("company").document("profile")
            .set(companyData)
            .addOnSuccessListener { Log.d("FirebaseSeeder", "Company profile successfully written!") }
            .addOnFailureListener { e -> Log.w("FirebaseSeeder", "Error writing company profile", e) }

        // 2. Seed Employees
        val employees = listOf(
            hashMapOf(
                "name" to "Rahul Sharma",
                "role" to "Store Manager",
                "empid" to "E001",
                "doj" to "2023-01-15",
                "deputedAt" to "Zepto Limited",
                "bank" to "345678123456",
                "pan" to "ABCDE1234F",
                "bankname" to "State Bank of India",
                "ifsc" to "SBIN0001234",
                "store" to "HBL Keshwapur",
                "payMonth" to 9,
                "payYear" to 2026,
                "customDaysInMonth" to 31,
                "paidDays" to 30,
                "lwp" to 1,
                "refundDays" to 0,
                "basic" to 25000,
                "attendance" to 1000,
                "performance" to 2000,
                "festivalBonus" to 0,
                "dq" to 500,
                "otherDed" to 0,
                "groupInsurance" to 300,
                "remarks" to "1 day LWP deducted."
            ),
            hashMapOf(
                "name" to "Priya Desai",
                "role" to "Cashier",
                "empid" to "E002",
                "doj" to "2024-05-10",
                "deputedAt" to "Zepto Limited",
                "bank" to "987654321098",
                "pan" to "FGHIJ5678K",
                "bankname" to "HDFC Bank",
                "ifsc" to "HDFC0004321",
                "store" to "HBL Gokul Road",
                "payMonth" to 9,
                "payYear" to 2026,
                "customDaysInMonth" to 31,
                "paidDays" to 31,
                "lwp" to 0,
                "refundDays" to 0,
                "basic" to 18000,
                "attendance" to 1000,
                "performance" to 500,
                "festivalBonus" to 1000,
                "dq" to 0,
                "otherDed" to 0,
                "groupInsurance" to 300,
                "remarks" to "Full attendance bonus applied + Diwali Festival Bonus."
            ),
            hashMapOf(
                "name" to "Amit Kumar",
                "role" to "Delivery Executive",
                "empid" to "E003",
                "doj" to "2025-02-20",
                "deputedAt" to "Zepto Limited",
                "bank" to "112233445566",
                "pan" to "KLMNO1234P",
                "bankname" to "ICICI Bank",
                "ifsc" to "ICIC0001122",
                "store" to "HBL Vidyanagar",
                "payMonth" to 9,
                "payYear" to 2026,
                "customDaysInMonth" to 31,
                "paidDays" to 28,
                "lwp" to 3,
                "refundDays" to 0,
                "basic" to 15000,
                "attendance" to 500,
                "performance" to 0,
                "festivalBonus" to 0,
                "dq" to 100,
                "otherDed" to 0,
                "groupInsurance" to 300,
                "remarks" to "3 days LWP."
            )
        )

        employees.forEach { empData ->
            db.collection("employees")
                .add(empData)
                .addOnSuccessListener { documentReference ->
                    Log.d("FirebaseSeeder", "Employee added with ID: ${documentReference.id}")
                    
                    // 3. Seed a Salary Slip for this employee
                    val slipData = hashMapOf(
                        "empId" to documentReference.id,
                        "empName" to empData["name"],
                        "payMonth" to empData["payMonth"],
                        "payYear" to empData["payYear"],
                        "timestamp" to "2026-10-07T12:00:00.000Z",
                        "basic" to empData["basic"],
                        "attendance" to empData["attendance"],
                        "performance" to empData["performance"],
                        "festivalBonus" to empData["festivalBonus"],
                        "dq" to empData["dq"],
                        "otherDed" to empData["otherDed"],
                        "groupInsurance" to empData["groupInsurance"],
                        "lwp" to empData["lwp"],
                        "paidDays" to empData["paidDays"]
                    )

                    db.collection("slips").add(slipData)
                        .addOnSuccessListener { Log.d("FirebaseSeeder", "Slip added for ${empData["name"]}") }
                        .addOnFailureListener { e -> Log.w("FirebaseSeeder", "Error adding slip", e) }
                }
                .addOnFailureListener { e -> Log.w("FirebaseSeeder", "Error adding employee", e) }
        }
    }
}
