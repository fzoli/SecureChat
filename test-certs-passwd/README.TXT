A jelszó az összes tanúsítványhoz: asdfgh

A "big2" tanúsítvány kiállítója a "big" tanúsítvány, a többinek a "ca".
Ha a szervernek a "ca" van megadva kiállítónak, mindegyik tanúsítvánnyal
lehetséges a szerverhez kapcsolódni, kivéve a "big2"-vel; viszont
ha a kiállítónak a "big" van megadva, csak a "big2" és a "big2-server"
használható kapcsolat létrehozására.

Ez azért van így, mert mind a szerver, mind a kliens alkalmazás csakis a
megadott kiállítóval létrehozott tanúsítványokat fogadják el és ez abban
az esetben is így van, ha a kiállító által létrehozott tanúsítvánnyal
van aláírva egy másik tanúsítvány, ahogyan a "big2" esetében.
A lánc: ca -> big -> big2

Ha a "ca" a kiállító, a "big" érvényes, de a "big2" nem.
Ha a "big" a kiállító, a "big2" érvényes, de a "big" nem, mivel ő a kiállító.
Ez könnyen tesztelhető a szerver konfig átírásával és a kliens átállításával.

A "big2" tanúsítványok tesztelésére értelemszerűen a konfiguráció:
Mindkét alkalmazás esetén közös a kiállító: big
A szerver alkalmazás tanúsítványa: big2-server
A kliens alkalmazás tanúsítványa: big2
