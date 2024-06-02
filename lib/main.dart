import 'dart:async';

import 'package:flutter/material.dart';
import 'package:sms_auto_fill/sms_auto_fill.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter SMS Auto Fill',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: 'SMS Auto Fill'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  dynamic _code;
  dynamic _signature;

  @override
  void initState() {
    super.initState();
    _asyncInit();
  }

  Future<void> _asyncInit() async {
    _signature = await SmsAutoFill.getAppSignature();
    print(_signature);
    await Future.delayed(const Duration(seconds: 1));
    _code = await SmsAutoFill.listenForOTPCode();
    print(_code);
    print('Async operation completed');
    setState(() {});
  }

  @override
  void dispose() {
    SmsAutoFill.stopListeningForOTPCode();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            const Text(
              'Signature:',
            ),
            Text(
              '$_signature',
              style: Theme.of(context).textTheme.headlineMedium,
            ),
            const Text(
              'OTP:',
            ),
            Text(
              '$_code',
              style: Theme.of(context).textTheme.headlineMedium,
            ),
          ],
        ),
      ),
    );
  }
}
