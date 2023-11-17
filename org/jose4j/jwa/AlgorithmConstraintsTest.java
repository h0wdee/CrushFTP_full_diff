/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.junit.Test
 */
package org.jose4j.jwa;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.lang.InvalidAlgorithmException;
import org.junit.Test;

public class AlgorithmConstraintsTest {
    @Test
    public void blacklist1() throws InvalidAlgorithmException {
        AlgorithmConstraints constraints = new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.BLACKLIST, "bad", "badder");
        constraints.checkConstraint("good");
    }

    @Test(expected=InvalidAlgorithmException.class)
    public void blacklist2() throws InvalidAlgorithmException {
        AlgorithmConstraints constraints = new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.BLACKLIST, "bad", "badder");
        constraints.checkConstraint("bad");
    }

    @Test(expected=InvalidAlgorithmException.class)
    public void blacklist3() throws InvalidAlgorithmException {
        AlgorithmConstraints constraints = new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.BLACKLIST, "bad", "badder");
        constraints.checkConstraint("badder");
    }

    @Test(expected=InvalidAlgorithmException.class)
    public void blacklistNone() throws InvalidAlgorithmException {
        AlgorithmConstraints constraints = new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.BLACKLIST, "none");
        constraints.checkConstraint("none");
    }

    @Test(expected=InvalidAlgorithmException.class)
    public void whitelist1() throws InvalidAlgorithmException {
        AlgorithmConstraints constraints = new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST, "good", "gooder", "goodest");
        constraints.checkConstraint("bad");
    }

    @Test(expected=InvalidAlgorithmException.class)
    public void whitelist2() throws InvalidAlgorithmException {
        AlgorithmConstraints constraints = new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST, "good", "gooder", "goodest");
        constraints.checkConstraint("also bad");
    }

    @Test
    public void whitelist3() throws InvalidAlgorithmException {
        AlgorithmConstraints constraints = new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST, "good", "gooder", "goodest");
        constraints.checkConstraint("good");
        constraints.checkConstraint("gooder");
        constraints.checkConstraint("goodest");
    }

    @Test
    public void noRestrictions() throws InvalidAlgorithmException {
        String[] algs;
        AlgorithmConstraints constraints = AlgorithmConstraints.NO_CONSTRAINTS;
        String[] stringArray = algs = new String[]{"none", "HS256", "HS512", "RS256", "RS512", "ES256", "something", "A128KW", "A256KW", "dir", "etc,", "etc."};
        int n = algs.length;
        int n2 = 0;
        while (n2 < n) {
            String alg = stringArray[n2];
            constraints.checkConstraint(alg);
            ++n2;
        }
    }
}

