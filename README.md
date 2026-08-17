# Pure-Java ISO 8583 + EMV Parser & Builder

A complete pure-Java ISO 8583 + EMV parser/builder project using **Java 17**, **Maven**, **Jackson**, and **JUnit 5**.

This project uses a custom parser instead of libraries like `j8583` or `jPOS` to provide full, low-level control over:
- TCP framing
- 2-byte and 4-byte length headers
- TPDU
- Binary bitmaps
- Dynamic field definitions
- Binary fields
- Field 55 & 127 EMV BER-TLV parsing
- Nested EMV TLVs
- Message building and framing

> [!NOTE]
> There is no single universal definition for ISO 8583 fields 1–128. Definitions vary between ISO 8583:1987, 1993, networks, switches, acquirers, and issuers. This implementation provides a practical **ISO 8583:1987-style profile** for positions 1–128 and allows definitions to be overridden dynamically.

---

## Project Structure
```text
ISO8583_Utility/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/iso8583_utility/iso8583/
    │   │   ├── model/
    │   │   │   ├── IsoMessageDto.java
    │   │   │   ├── IsoFieldDto.java
    │   │   │   ├── EmvTagDto.java
    │   │   │   └── IsoFieldDefinition.java
    │   │   ├── parser/
    │   │   │   ├── IsoParseException.java
    │   │   │   ├── FramedMessage.java
    │   │   │   ├── FramingHandler.java
    │   │   │   ├── EmvTlvParser.java
    │   │   │   └── Iso8583Parser.java
    │   │   ├── builder/
    │   │   │   └── Iso8583Builder.java
    │   │   ├── config/
    │   │   │   └── Iso8583FieldDefinitions.java
    │   │   └── iso_utility.java
    │   └── resources/
    │       └── iso8583-config.xml
    └── test/
        └── java/com/example/iso8583/
            └── Iso8583ParserTest.java

Message Flow Architecture
The implementation is structured into four distinct processing layers:
TCP/IP byte stream
                                │
                                ▼
                      ┌──────────────────┐
                      │ FramingHandler   │
                      │                  │
                      │ Length Header    │
                      │ 2 / 4 bytes      │
                      │ TPDU             │
                      └────────┬─────────┘
                               │
                               ▼
                        ISO 8583 payload
                               │
                               ▼
                      ┌──────────────────┐
                      │ Iso8583Parser    │
                      │                  │
                      │ MTI              │
                      │ Bitmap           │
                      │ Fields 1-128     │
                      │ LLVAR/LLLVAR     │
                      └────────┬─────────┘
                               │
                         Field 55 / 127
                               │
                               ▼
                      ┌──────────────────┐
                      │ EmvTlvParser     │
                      │                  │
                      │ Tag              │
                      │ Length           │
                      │ Value            │
                      │ Nested TLVs      │
                      └────────┬─────────┘
                               │
                               ▼
                           JSON / DTO

Example Processing Breakdown
ISO Payload Hierarchy

0200
  │
Primary Bitmap
  │
Secondary Bitmap (if required)
  │
Field 2
  │
Field 3
  │
Field 4
  │
...
Field 55
  │
03F... / EMV BER-TLV
  │
  ├── 9F26 -> Application Cryptogram
  ├── 9F10 -> Issuer Application Data
  ├── 82   -> Application Interchange Profile
  ├── 9F36 -> Application Transaction Counter
  └── 5F2A -> Transaction Currency Code

Conceptual Output (JSON)
{
  "55": [
    {
      "tag": "9F26",
      "length": 8,
      "value": "A1A2A3A4A5A6A7A8",
      "description": "Application Cryptogram"
    },
    {
      "tag": "9F10",
      "length": 7,
      "value": "06010A03A0B800",
      "description": "Issuer Application Data"
    },
    {
      "tag": "82",
      "length": 2,
      "value": "1800",
      "description": "Application Interchange Profile"
    },
    {
      "tag": "9F36",
      "length": 2,
      "value": "0001",
      "description": "Application Transaction Counter"
    },
    {
      "tag": "5F2A",
      "length": 2,
      "value": "0356",
      "description": "Transaction Currency Code"
    }
  ]
}

POS & Switch Layer Architecture
For POS terminal or payment switch implementations, the integration pipeline operates as follows:

TCP Server / Netty
        │
        ▼
FramingHandler
        │
        ▼
ISO8583 Parser
        │
        ├── MTI validation
        ├── Bitmap validation
        ├── Field validation
        ├── Field 55 EMV parser
        ├── Field 52 PIN block
        ├── Field 35 Track-2
        └── Field 127 private subfields
        │
        ▼
Transaction Router
        │
        ├── 0100 Authorization
        ├── 0200 Financial
        ├── 0400 Reversal
        ├── 0800 Network Management
        └── 0810 Network Response
        │
        ▼
Mock Switch / Host
        │
        ▼
Iso8583Builder
        │
        ▼
FramingHandler
        │
        ▼
POS Terminal