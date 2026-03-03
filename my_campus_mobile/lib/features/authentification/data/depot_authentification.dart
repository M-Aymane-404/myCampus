import '../../../core/network/client_dio.dart';
import '../../../core/storage/stockage_securise.dart';
import 'service_authentification_api.dart';

class DepotAuthentification {
  final ServiceAuthentificationApi _serviceApi = ServiceAuthentificationApi();

  Future<Map<String, dynamic>> seConnecter(
    String email,
    String motDePasse,
  ) async {
    final result = await _serviceApi.seConnecter(email, motDePasse);

    final token = result['access_token']?.toString();
    if (token == null || token.isEmpty) {
      throw Exception("Réponse login invalide: access_token manquant");
    }

    ClientDio.instance.setAccessToken(token);

    await StockageSecurise.sauvegarderToken(token);

    final profil = await _serviceApi.recupererProfil(token);
    final authorities = (profil['authorities'] as List?)?.cast<String>() ?? [];
    final role = _extraireRole(authorities);

    if (role == null) {
      throw Exception(
        "Profil invalide: ROLE_* introuvable dans authorities: $authorities",
      );
    }

    await StockageSecurise.sauvegarderRole(role);

    return result;
  }

  Future<void> seDeconnecter() async {
    try {
      await _serviceApi.seDeconnecter().timeout(const Duration(seconds: 5));
    } catch (_) {
    } finally {
      await StockageSecurise.supprimerToken();
      await StockageSecurise.supprimerRole();
      ClientDio.instance.viderSessionLocal();
    }
  }

  Future<bool> estAuthentifie() async {
    final token = await StockageSecurise.recupererToken();
    if (token != null && token.isNotEmpty) {
      ClientDio.instance.setAccessToken(token); // ✅ rehydrate
      return true;
    }
    return false;
  }

  Future<String?> recupererRoleUtilisateur() async {
    return await StockageSecurise.recupererRole();
  }

  String? _extraireRole(List<String> authorities) {
    for (final a in authorities) {
      if (a.startsWith('ROLE_')) return a.replaceFirst('ROLE_', '');
    }
    return null;
  }
}
