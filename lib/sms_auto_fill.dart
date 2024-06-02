import 'package:flutter/services.dart';

class SmsAutoFill {
  static const platform = MethodChannel('com.kennistec/sms_autofill');

  static Future<dynamic> getAppSignature() async {
    return await platform.invokeMethod("getAppSignature");
  }

  static Future<dynamic> listenForOTPCode() async {
    return await platform.invokeMethod("listenForCode");
  }

  static Future<void> stopListeningForOTPCode() async {
    await platform.invokeMethod("stopListeningForOTPCode");
  }
}
