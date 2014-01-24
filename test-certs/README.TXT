Ezek a tanúsítványok kizárólag tesztelés céljára lettek gyártva.
Valós felhasználás esetén tilos nyilvánosan kiadni a titkos kulcsot, különben az egész SSL semmit nem ér.

A tanúsítványokat GNU Debian rendszeren generáltam az easy-rsa segítségével, amit az OpenVPN beállításakor tanultam meg használni.

Az easy-rsa csomagból telepítés után a /usr/share/doc/openvpn/examples/easy-rsa/2.0 könyvtárból érhető el.
Erősen ajánlott az eredeti szerkezetet nem piszkálni és az egész könyvtárról másolatot készíteni és a generálásra azt használni.

A könyvtárban található egy vars nevű fájl.
A legvégén én ezeket az értékeket állítottam be:
export KEY_COUNTRY="HU"
export KEY_PROVINCE="Pest"
export KEY_CITY="Budapest"
export KEY_ORG="Mobile-RC Test"
export KEY_EMAIL="info@mobilerc.test"
export KEY_CN=""
export KEY_NAME=""
export KEY_OU="Test certificate"
export PKCS11_MODULE_PATH=changeme
export PKCS11_PIN=1234

A vars egy scriptfájl, ami változókat állít be a tanúsítványok egyszerűbb generálásához. Valójában ezekre az értékekre minden generáláskor rákérdez a program, de alapértelmezett értéknek az itt megadottak állítódnak be. (Az utolsó két érték lényegtelen most.)

Miután elmentettük, le kell futtatni a scriptet: source ./vars
Ezek után már használhatóak az alábbi parancsok:
clean-all, build-ca, build-key-server, build-key

Hogy biztosan tiszta lappal induljunk a generálás előtt: ./clean-all

Elsőként kell egy kiállító (CA), ami a tanúsítványokat generálni fogja: ./build-ca
A vars fájlban üresen hagytam a CN mezőt, hogy generálódjon az érték, így végig entert nyomkodtam, mert minden adat stimmelt.
Challenge password mezőt is üresen hagytam, ahogyan az opcionális cégnevet is. (nincs szükség CSR-re, mert nem megyek hivatalos CA-hoz)
Ez után mindkét eldöntendő kérdésre Y volt a válasz és el is készült a 10 évig érvényes CA.

Minden kulcs a keys könyvtárba generálódik, így abban található meg ezek után a ca.crt (nyilvános kulcs) és a ca.key (titkos kulcs) fájl is.

Most jöhet a szerver tanúsítvány generálása a hídhoz: ./build-key-server bridge
Megint végig enter, majd két igen. Ez esetben bridge.crt és bridge.key a két fájl neve és a CN mezőbe is a bridge az alapértelmezés.

Már csak a kliens tanúsítványok maradtak: ./build-key controller
Az eljárás ugyan az, mint a build-key-server esetén.

Ha jelszóval védett tanúsítványt kell generálni a kliensekhez: ./build-key-pass
Ez esetben a tanúsítvány csak az itt bekért jelszó megadása után használható.

Ha a szerverhez kell a jelszóvédett tanúsítvány, akkor létre kell hozni a script fájlt hozzá.
A legegyszerűbb, ha a build-key-pass fájlról készítünk egy másolatot (mondjuk build-key-pass-server néven) és a parancshoz hozzáadjuk a --server paramétert így:
"$EASY_RSA/pkitool" --interact --server --pass $*

Ha a szervernek egy klienshez kiállított tanúsítványt állít be a felhasználó, a szerver hiba nélkül elindul, de egy kliens se fog tudni kapcsolódni
a szerverhez. Javaban a következő kivétel keletkezik az SSL kézfogás közben: Extended key usage does not permit use for TLS server authentication.
Ekkor már szerver oldalon is kivétel keletkezik, de ott egyszerűen annyit mond a kivétel, hogy ismeretlen tanúsítvány.

Hasonló dolog történik akkor is, ha a kliensnek állítanak be szerverhez kiállított tanúsítványt.
A kapcsolat létrehozásakor a szerver oldalon keletkező kivétel: Extended key usage does not permit use for TLS client authentication.

Megjegyzés:
Bármikor generálható még több tanúsítvány, de szükség van hozzá a ca.crt és ca.key fájlra is, ezért jól el kell tenni védett helyre, különben az összes
kiállított tanúsítványt le kell cserélni, mivel a kiállító megváltozik és a program csak egy kiállítóban bízik meg.
