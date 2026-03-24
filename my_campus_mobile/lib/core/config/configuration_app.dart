class ConfigurationApp {
  static const String apiBaseUrl = 'http://100.88.209.222:8080';
  static const Duration connexionTimeout = Duration(seconds: 30);
  static const Duration receptionTimeout = Duration(seconds: 30);
  static const Map<String, String> entetesParDefaut = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  };
}
