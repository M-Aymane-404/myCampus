import '../../../core/storage/stockage_securise.dart';
import 'service_authentification_api.dart';

class DepotAuthentification {
  final ServiceAuthentificationApi _serviceApi = ServiceAuthentificationApi();

  Future<Map<String, dynamic>> seConnecter(
    String email,
    String motDePasse,
  ) async {
    final result = await _serviceApi.seConnecter(email, motDePasse);

    if (result['token'] != null) {
      await StockageSecurise.sauvegarderToken(result['token']);
    }

    if (result['user'] != null) {
      await StockageSecurise.sauvegarderUtilisateur(result['user'].toString());
    }

    return result;
  }

  Future<void> seDeconnecter() async {
    final token = await StockageSecurise.recupererToken();
    if (token != null) {
      await _serviceApi.seDeconnecter(token);
    }
    await StockageSecurise.supprimerToken();
    await StockageSecurise.supprimerUtilisateur();
  }

  Future<bool> estAuthentifie() async {
    final token = await StockageSecurise.recupererToken();
    return token != null;
  }

  Future<String?> recupererRoleUtilisateur() async {
    final utilisateur = await StockageSecurise.recupererUtilisateur();
    if (utilisateur != null) {
      // Parse user data to extract role
      return 'ETUDIANT'; // Placeholder
    }
    return null;
  }
}
