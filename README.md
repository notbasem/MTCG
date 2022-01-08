# MTCG Protocol

## Design
Als Datenbank wurde eine PostgreSql DB verwendet.

Das Projekt ist in 3 Bereiche unterteilt:
- Controller
- DatabaseAccess
- Models

Diese 3 Bereiche wurden anhand des curl-scripts stetig erweitert.
So wurden zu aller erst die Registrierung der User entwickelt, anschließend
der Login der User, danach das Erstellen der Packages etc.

Grundsätzlich läuft der API-Request immer gleich ab. Es wird ein neuer Thread mit
einem ClientHandler erstellt, der den Request verarbeitet (Methode, URI, BODY, etc.
herauslesen) und dann anhand der URI das routing übernimmt.

Beim Routing wird die URI, die Methode und die Authorization berücksichtigt und anschließend
zum entsprechenden Controller weitergeleitet. Der Controller leitet die Anfrage an die
entsprechende DBAccess-Klasse weiter. In der DBAccess-Klasse wird der Request bearbeitet und in die
Datenbank eingetragen. Anschließend liefert die DBAccess-Klasse eine HTTP-Response an den Controller zurück.
Vom Controller aus wird diese Response dann an den Client zurückgeschickt und der Request ist zu ende.

## Probleme
### Token management:
Es wäre wahrscheinlich besser gewesen den Token als primary key zu nehmen, da
dieser sonst ständig in die User-Id umgewandelt werden muss.

### Cross-Communication zwischen DB-Access Klassen
Z.b.: Soll ich in der CardAccess-Klasse direkt eine User-Abfrage machen (z.b.: UserId), oder
soll das ganze über die UserAccess-Klasse geschehen. Ich habe mich im endeffekt dazu
entschieden, den kompletten User-Zugriff nur über die UserAccess-Klasse zu machen. Das heißt,
es wurde dann in der CardAccess-Klasse folgendes gemacht um die User-Id zu bekommen:

String userId = new UserAccess().getId(token);

### Curl-Script nicht ausführbar auf MacOS
Habe alle Curl-Script-Requests in ein API-Request-Tool geschrieben und
diese dann als Scenario nacheinander ausführen lassen.

## Unit-Tests
Es wurden ca. 20 Unit-Tests angefertigt, die den Userzugriff und die Package-/Carderstellung
testen. Die Test wurden so gewählt, weil die beiden Funktionen der Grundbaustein
für eine funktionierende Applikation sind. Ohne Userzugriff und ohne Packages/Karten funktioniert der Rest
der Anforderungen auch nicht.

## Zeitaufwand
Ca. 60h

## Github
https://github.com/notbasem/MTCG

##### DB Zugriff (lol das ist nur für mich schaut weg):
pg_ctl -D /usr/local/var/postgres start

pg_ctl -D /usr/local/var/postgres stop