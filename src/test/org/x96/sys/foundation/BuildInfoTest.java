package org.x96.sys.foundation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class BuildInfoTest {

    @Test
    public void testVersionIsNotNull() {
        assertNotNull(BuildInfo.VERSION);
        assertFalse(BuildInfo.VERSION.isEmpty());
    }

    @Test
    public void testBuildDateIsNotNull() {
        assertNotNull(BuildInfo.BUILD_DATE);
        assertFalse(BuildInfo.BUILD_DATE.isEmpty());
    }

    @Test
    public void testBuildUserIsNotNull() {
        assertNotNull(BuildInfo.BUILD_USER);
        assertFalse(BuildInfo.BUILD_USER.isEmpty());
    }

    @Test
    public void testVersionMajorIsNotNull() {
        assertNotNull(BuildInfo.VERSION_MAJOR);
        assertFalse(BuildInfo.VERSION_MAJOR.isEmpty());
    }

    @Test
    public void testVersionMinorIsNotNull() {
        assertNotNull(BuildInfo.VERSION_MINOR);
        assertFalse(BuildInfo.VERSION_MINOR.isEmpty());
    }

    @Test
    public void testVersionPatchIsNotNull() {
        assertNotNull(BuildInfo.VERSION_PATCH);
        assertFalse(BuildInfo.VERSION_PATCH.isEmpty());
    }

    @Test
    public void testGetFullVersion() {
        String fullVersion = BuildInfo.getFullVersion();
        assertNotNull(fullVersion);
        assertTrue(fullVersion.contains(BuildInfo.VERSION));
        assertTrue(fullVersion.contains(BuildInfo.BUILD_DATE));
        assertTrue(fullVersion.contains(BuildInfo.BUILD_USER));
        assertTrue(fullVersion.contains("built on"));
        assertTrue(fullVersion.contains("by"));
    }

    @Test
    public void testVersionFormat() {
        // Testa se a versão segue um formato básico (vX.Y.Z ou X.Y.Z com possível
        // sufixo)
        assertTrue(BuildInfo.VERSION.matches("v?\\d+\\.\\d+\\.\\d+.*"));
    }

    @Test
    public void testVersionComponents() {
        // Testa se os componentes da versão são números
        assertTrue(BuildInfo.VERSION_MAJOR.matches("\\d+"));
        assertTrue(BuildInfo.VERSION_MINOR.matches("\\d+"));
        assertTrue(BuildInfo.VERSION_PATCH.matches("\\d+"));
    }

    @Test
    public void testBuildDateFormat() {
        // Testa se a data de build segue um formato básico (YYYY-MM-DD HH:MM:SS)
        assertTrue(BuildInfo.BUILD_DATE.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }
}
