package com.iso8583_utility.iso8583.config;

import com.iso8583_utility.iso8583.model.IsoFieldDefinition;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Iso8583FieldDefinitions {

        private Iso8583FieldDefinitions() {
        }

        public static Map<Integer, IsoFieldDefinition> create() {

                Map<Integer, IsoFieldDefinition> m = new HashMap<>();

                /*
                 * ISO 8583:1987-style practical profile.
                 *
                 * Note:
                 * Network implementations may override these
                 * definitions.
                 */

                putFixed(m, 2, "n", 0); // LLVAR below
                m.put(2, IsoFieldDefinition.llvar(2, "n", 19));

                putFixed(m, 3, "n", 6);
                putFixed(m, 4, "n", 12);
                putFixed(m, 5, "n", 12);
                putFixed(m, 6, "n", 12);
                putFixed(m, 7, "n", 10);
                putFixed(m, 8, "n", 8);
                putFixed(m, 9, "n", 8);
                putFixed(m, 10, "n", 8);
                putFixed(m, 11, "n", 6);
                putFixed(m, 12, "n", 6);
                putFixed(m, 13, "n", 4);
                putFixed(m, 14, "n", 4);
                putFixed(m, 15, "n", 4);
                putFixed(m, 16, "n", 4);
                putFixed(m, 17, "n", 4);
                putFixed(m, 18, "n", 4);
                putFixed(m, 19, "n", 3);
                putFixed(m, 20, "n", 3);
                putFixed(m, 21, "n", 3);
                putFixed(m, 22, "n", 3);
                putFixed(m, 23, "n", 3);
                putFixed(m, 24, "n", 3);
                putFixed(m, 25, "n", 2);
                putFixed(m, 26, "n", 2);
                putFixed(m, 27, "n", 1);

                putFixed(m, 28, "x+n", 8);
                putFixed(m, 29, "x+n", 8);
                putFixed(m, 30, "x+n", 8);

                m.put(31, IsoFieldDefinition.llvar(31, "ans", 99));
                m.put(32, IsoFieldDefinition.llvar(32, "n", 11));
                m.put(33, IsoFieldDefinition.llvar(33, "n", 11));
                m.put(34, IsoFieldDefinition.llvar(34, "ans", 28));
                m.put(35, IsoFieldDefinition.llvar(35, "z", 37));
                m.put(36, IsoFieldDefinition.lllvar(36, "z", 104));

                putFixed(m, 37, "ans", 12);
                putFixed(m, 38, "ans", 6);
                putFixed(m, 39, "ans", 2);
                putFixed(m, 40, "ans", 3);
                putFixed(m, 41, "ans", 8);
                putFixed(m, 42, "ans", 15);
                putFixed(m, 43, "ans", 40);

                m.put(44, IsoFieldDefinition.llvar(44, "ans", 25));
                m.put(45, IsoFieldDefinition.llvar(45, "ans", 76));
                m.put(46, IsoFieldDefinition.lllvar(46, "ans", 999));
                m.put(47, IsoFieldDefinition.lllvar(47, "ans", 999));
                m.put(48, IsoFieldDefinition.lllvar(48, "ans", 999));

                putFixed(m, 49, "ans", 3);
                putFixed(m, 50, "ans", 3);
                putFixed(m, 51, "ans", 3);

                putFixed(m, 52, "b", 8);
                putFixed(m, 53, "n", 16);

                m.put(54, IsoFieldDefinition.lllvar(54, "ans", 120));
                m.put(55, IsoFieldDefinition.lllvar(55, "b", 999));
                m.put(56, IsoFieldDefinition.lllvar(56, "ans", 999));
                m.put(57, IsoFieldDefinition.lllvar(57, "ans", 999));
                m.put(58, IsoFieldDefinition.lllvar(58, "ans", 999));
                m.put(59, IsoFieldDefinition.lllvar(59, "ans", 999));
                m.put(60, IsoFieldDefinition.lllvar(60, "ans", 999));
                m.put(61, IsoFieldDefinition.lllvar(61, "ans", 999));
                m.put(62, IsoFieldDefinition.lllvar(62, "ans", 999));
                m.put(63, IsoFieldDefinition.lllvar(63, "ans", 999));

                putFixed(m, 64, "b", 8);
                putFixed(m, 65, "b", 1);
                putFixed(m, 66, "n", 1);
                putFixed(m, 67, "n", 2);
                putFixed(m, 68, "n", 3);
                putFixed(m, 69, "n", 3);
                putFixed(m, 70, "n", 3);
                putFixed(m, 71, "n", 4);

                m.put(72, IsoFieldDefinition.lllvar(72, "ans", 999));

                putFixed(m, 73, "n", 6);
                putFixed(m, 74, "n", 10);
                putFixed(m, 75, "n", 10);
                putFixed(m, 76, "n", 10);
                putFixed(m, 77, "n", 10);
                putFixed(m, 78, "n", 10);
                putFixed(m, 79, "n", 10);
                putFixed(m, 80, "n", 10);
                putFixed(m, 81, "n", 10);
                putFixed(m, 82, "n", 12);
                putFixed(m, 83, "n", 12);
                putFixed(m, 84, "n", 12);
                putFixed(m, 85, "n", 12);
                putFixed(m, 86, "n", 16);
                putFixed(m, 87, "n", 16);
                putFixed(m, 88, "n", 16);
                putFixed(m, 89, "n", 16);
                putFixed(m, 90, "n", 42);
                putFixed(m, 91, "ans", 1);
                putFixed(m, 92, "ans", 1);

                m.put(93, IsoFieldDefinition.llvar(93, "ans", 11));

                putFixed(m, 94, "ans", 7);
                putFixed(m, 95, "ans", 42);
                putFixed(m, 96, "b", 8);
                putFixed(m, 97, "x+n", 16);
                putFixed(m, 98, "ans", 25);

                m.put(99, IsoFieldDefinition.llvar(99, "n", 11));
                m.put(100, IsoFieldDefinition.llvar(100, "n", 11));
                m.put(101, IsoFieldDefinition.llvar(101, "ans", 17));
                m.put(102, IsoFieldDefinition.llvar(102, "ans", 28));
                m.put(103, IsoFieldDefinition.llvar(103, "ans", 28));

                for (int field = 104; field <= 127; field++) {
                        m.put(
                                        field,
                                        IsoFieldDefinition.lllvar(
                                                        field,
                                                        "ans",
                                                        999));
                }

                /*
                 * Field 127 is network-specific.
                 *
                 * It is deliberately represented as LLLVAR ans
                 * here because private field 127 layouts vary.
                 *
                 * If your switch uses binary Field 127:
                 * replace this definition with:
                 *
                 * IsoFieldDefinition.lllvar(127, "b", 999)
                 */
                m.put(127, IsoFieldDefinition.lllvar(127, "ans", 999));

                putFixed(m, 128, "b", 8);
                return Collections.unmodifiableMap(m);
        }

        private static void putFixed(Map<Integer, IsoFieldDefinition> map, int field, String type, int length) {
                map.put(field, IsoFieldDefinition.fixed(field, type, length));
        }
}