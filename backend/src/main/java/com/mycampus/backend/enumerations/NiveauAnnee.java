package com.mycampus.backend.enumerations;

import com.fasterxml.jackson.annotation.JsonCreator;

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

    @JsonCreator
    public static NiveauAnnee fromJson(String valeur) {
        if (valeur == null) {
            return null;
        }

        String normalizedValue = valeur.trim();

        for (NiveauAnnee n : values()) {
            if (n.name().equalsIgnoreCase(normalizedValue) || n.valeur.equalsIgnoreCase(normalizedValue)) {
                return n;
            }
        }

        throw new IllegalArgumentException("Niveau invalide: " + valeur);
    }

    public static NiveauAnnee fromValeur(String valeur) {
        return fromJson(valeur);
    }
}
