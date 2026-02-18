package com.mycampus.backend.enumerations;

public enum NiveauAnnee {

    A1("1A"),
    A2("2A"),
    A3("3A"),
    A5("5A"),
    A6("6A"),
    A7("7A");

    private final String valeur;

    NiveauAnnee(String valeur) {
        this.valeur = valeur;
    }

    public String getValeur() {
        return valeur;
    }

    public static NiveauAnnee fromValeur(String valeur) {
        for (NiveauAnnee n : values()) {
            if (n.valeur.equalsIgnoreCase(valeur)) {
                return n;
            }
        }
        throw new IllegalArgumentException("Niveau invalide: " + valeur);
    }
}
