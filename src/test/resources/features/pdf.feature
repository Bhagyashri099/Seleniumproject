 Feature: PDF Detail Validation
 @ignore
 Scenario: Validate text, font, and color of multi-page PDF
   
 Given user has the PDF file "sample.pdf"
 When user validates all pages of the PDF
 And test data is loaded from "TestData.xlsx"
 Then all text, font types, and colors should match the test data sheet
 