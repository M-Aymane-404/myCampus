import 'package:dio/dio.dart';

import '../config/configuration_app.dart';

class ClientDio {
  static final ClientDio _instance = ClientDio._internal();
  factory ClientDio() => _instance;

  late final Dio dio;

  ClientDio._internal() {
    dio = Dio(
      BaseOptions(
        baseUrl: ConfigurationApp.apiBaseUrl,
        connectTimeout: ConfigurationApp.connexionTimeout,
        receiveTimeout: ConfigurationApp.receptionTimeout,
        headers: ConfigurationApp.entetesParDefaut,
      ),
    );
  }
}
