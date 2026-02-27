import 'package:dio/dio.dart';

class IntercepteurAuthentification extends Interceptor {
  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) {
    // Add authentication token to headers
    super.onRequest(options, handler);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) {
    // Handle authentication errors
    super.onError(err, handler);
  }
}
