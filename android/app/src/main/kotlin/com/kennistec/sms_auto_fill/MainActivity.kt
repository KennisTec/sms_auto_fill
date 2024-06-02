package com.kennistec.sms_auto_fill


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine

import io.flutter.plugin.common.MethodChannel
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;


class MainActivity : FlutterActivity() {

    private val SmsChannel = "com.kennistec/sms_autofill"
    private var receiver: MySMSBroadcastReceiver? = null
    var sms: String? = null
    var pendingResult: MethodChannel.Result? = null
    private lateinit var context: Context
    private var Tag: String = "MainActivity android"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        context = applicationContext
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, SmsChannel)
            .setMethodCallHandler { call, result ->
                // Implement your method handling logic here
                when (call.method) {
                    "listenForCode" -> {
                        pendingResult = result;
                        receiver = MySMSBroadcastReceiver()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            context.registerReceiver(
                                receiver,
                                IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION),
                                RECEIVER_NOT_EXPORTED
                            )
                        } else {
                            context.registerReceiver(
                                receiver,
                                IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
                            )
                        }
                        startListening()
                    }

                    "stopListeningForOTPCode" -> {
                        // Your method implementation
                        unregister(receiver)
                        result.success(null)
                    }

                    "getAppSignature" -> {
                        Log.i(Tag, "getAppSignature: started")
                        val signature = AppSignatureHelper(context).getAppSignatures()[0]
                        Log.i(Tag, "getAppSignature: $signature")
                        result.success(signature);
                    }

                    else -> {
                        result.notImplemented()
                    }
                }
            }
    }

    private fun unregister(receiver: BroadcastReceiver?) {

        receiver?.let {
            safeExecute {
                context.unregisterReceiver(it)
            }
        }
    };


    private fun ignoreIllegalState(fn: () -> Unit) {
        try {
            fn()
        } catch (e: IllegalStateException) {
            Log.d(
                Tag,
                "ignoring exception: $e. See https://github.com/flutter/flutter/issues/29092 for details."
            )
        }
    }

    private fun safeExecute(fn: () -> Unit) {
        try {
            fn()
        } catch (e: Exception) {
            Log.d(
                Tag,
                "ignoring exception: $e."
            )
        }
    }

    private fun startListening() {
        val client = SmsRetriever.getClient(context)
        val task = client.startSmsRetriever()
        task.addOnSuccessListener {
            Log.i(Tag, "task started")
        }

        task.addOnFailureListener {
            Log.i(Tag, "task starting failed")

        }
    }


    inner class MySMSBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
                val extras = intent.extras
                val status = extras!!.get(SmsRetriever.EXTRA_STATUS) as Status
                when (status.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        // Get SMS message contents
                        sms = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String

                        ignoreIllegalState {
                            val regex = Regex("\\b\\d{6}\\b")
                            val matches = regex.findAll(sms!!)
                            val result = matches.map { it.value }.toList()
                            pendingResult?.success(result[0])
                        }
                    }

                    CommonStatusCodes.TIMEOUT -> {
                    }
                }
            }
        }
    }
}
