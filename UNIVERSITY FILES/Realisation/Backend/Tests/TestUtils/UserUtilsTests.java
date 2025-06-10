package com.swisscom.travelmate.engine.UtilsTests;

import com.swisscom.travelmate.engine.modules.travelrequest.model.Country;
import org.junit.jupiter.api.Test;

import static com.swisscom.travelmate.engine.shared.util.DepartmentUtils.getCountryFromDepartment;
import static com.swisscom.travelmate.engine.shared.util.UserUtils.parseEmployeeNr;
import static org.junit.jupiter.api.Assertions.*;

class UserUtilsTests {

    @Test
    void testGetCountryFromDepartment_NL() {
        // Test case for a Dutch department code
        String department = "SUB-SDC-NL-001";
        Country result = getCountryFromDepartment(department);
        assertEquals(Country.NL, result, "Expected NL country for department code containing 'SUB-SDC-NL'");
    }

    @Test
    void testGetCountryFromDepartment_CH() {
        // Test case for a Swiss department code
        String department = "SUB-SDC-LV-001";
        Country result = getCountryFromDepartment(department);
        assertEquals(Country.LV, result, "Expected CH country for department code containing 'SUB-SDC-LV'");
    }

    @Test
    void testGetCountryFromDepartment_Invalid() {
        // Test case for an invalid department code
        String department = "SUB-SDC-DE-001";
        Country result = getCountryFromDepartment(department);
        assertEquals(Country.CH, result, "Expected CH country for department code not containing 'SUB-SDC-NL' or 'SUB-SDC-LV'");
    }

    @Test
    void testGetCountryFromDepartment_Null() {
        // Test case for null department
        String department = null;
        Country result = getCountryFromDepartment(department);
        assertNull(result, "Expected null for a null department");
    }

    @Test
    void testParseEmployeeNr_LessThan7Digits() {
        // Test case for employee number with less than 7 digits
        String employeeNr = "123";
        String result = parseEmployeeNr(employeeNr);
        assertEquals("0000123", result, "Expected '0000123' for employee number '123'");
    }

    @Test
    void testParseEmployeeNr_Exactly7Digits() {
        // Test case for employee number with exactly 7 digits
        String employeeNr = "1234567";
        String result = parseEmployeeNr(employeeNr);
        assertEquals("1234567", result, "Expected '1234567' for employee number '1234567'");
    }

    @Test
    void testParseEmployeeNr_MoreThan7Digits() {
        // Test case for employee number with more than 7 digits
        String employeeNr = "123456789";
        String result = parseEmployeeNr(employeeNr);
        assertEquals("123456789", result, "Expected '123456789' for employee number '123456789'");
    }

    @Test
    void testParseEmployeeNr_EmptyString() {
        // Test case for employee number as empty string
        String employeeNr = "";
        String result = parseEmployeeNr(employeeNr);
        assertEquals("0000000", result, "Expected '0000000' for empty employee number");
    }
}