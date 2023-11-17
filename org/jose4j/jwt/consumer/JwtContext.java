/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.jwt.consumer;

import java.util.List;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwx.JsonWebStructure;

public class JwtContext {
    private String jwt;
    private JwtClaims jwtClaims;
    private List<JsonWebStructure> joseObjects;

    public JwtContext(JwtClaims jwtClaims, List<JsonWebStructure> joseObjects) {
        this.jwtClaims = jwtClaims;
        this.joseObjects = joseObjects;
    }

    public JwtContext(String jwt, JwtClaims jwtClaims, List<JsonWebStructure> joseObjects) {
        this.jwt = jwt;
        this.jwtClaims = jwtClaims;
        this.joseObjects = joseObjects;
    }

    public JwtClaims getJwtClaims() {
        return this.jwtClaims;
    }

    public List<JsonWebStructure> getJoseObjects() {
        return this.joseObjects;
    }

    public String getJwt() {
        return this.jwt;
    }
}

