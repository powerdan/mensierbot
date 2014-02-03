<?php
$heute = date("d.m.Y");


$db = new SQLite3('../priv/mensierbot/data.db');

$sql = "SELECT * FROM times WHERE day = '" . $heute . "' ORDER BY time ASC;";

$result = $db->query($sql);

$times = array();

$users = array();


while ($res = $result->fetchArray(SQLITE3_ASSOC)) {
    if (!in_array($res['user'], $users)) {
        $users[] = $res['user'];
    }
    $times[$res['time']][] = $res['user'];
}
?>
<html>
    <head>
        <title>MensierBot</title>
    </head>
    <body>
        <h2>MensierBot Webinterface</h2>
        <table border="1">
            <tr>
                <th>Uhrzeit</th>
                <th colspan="<?php echo count($users); ?>">Interessenten</th>
            </tr>
            <?php
            foreach ($times as $time => $user) {
                echo "
	<tr>
		<td>" . $time . "</td>";
                for ($i = 0; $i < count($users); $i++) {
                    if(in_array($users[$i], $user))
                    {
                        echo "<td>" . $users[$i] . "</td>";
                    }
                    else
                    {
                        echo "<td>&nbsp;&</td>";
                    }
                }
                echo "	</tr>
";
            }
            ?>
        </table>
        <h3>Referenz</h3>
        <table border="1">
            <tr>
                <th>Befehl</th>
                <th>Beispiel</th>
                <th>Erl&auml;terung</th>
            </tr>
            <tr>
                <td>status</td>
                <td><pre>@MensierBot status</pre></td>
                <td>Zeigt die heutigen vorgeschlagenen Mensazeiten inkl. der Anzahl der sich beteiligen Personen</td>
            </tr>
            <tr>
                <td>status uhrzeit?</td>
                <td><pre>@MensierBot status 11:30
@MensierBot status 11:30 12:00</pre></td>
                <td>Zeigt die Personen, die zu den angegebenen Zeitr&auml;men in die Mensa gehen w&uuml;rden. Dabei kann eine 
                    beliebige Anzahl von Uhrzeiten angegeben werden.</td>
            </tr>
            <tr>
                <td>ja uhrzeit?</td>
                <td><pre>@MensierBot ja 11:30
@MensierBot ja 11:30 12:00 13:30</pre></td>
                <td>Sagt f&uuml;r die angegebenen Uhrzeiten zu</td>
            </tr>

            <tr>
                <td>nein uhrzeit?</td>
                <td><pre>@MensierBot nein 11:30
@MensierBot nein 11:30 12:00 13:30</pre></td>
                <td>Sagt f&uuml;r die angegebenen Uhrzeiten ab. Ist nur erforderlich, wenn man vorher zugesagt hat</td>
            </tr>

            <tr>
                <td>nein alle</td>
                <td><pre>@MensierBot nein alle</pre></td>
                <td>Sagt f&uuml;r den heutigen Tag zu allen Uhrzeiten ab.</td>
            </tr>
            <tr>
                <td>auswahl uhrzeit</td>
                <td><pre>@MensierBot auswahl 13:30</pre></td>
                <td>W&auml;hlt eine Mensazeit final aus</td>
            </tr>
            <tr>
                <td>psa text</td>
                <td><pre>@MensierBot psa Heute geht es ins Sonnendeck</pre></td>
                <td>Postet eine &ouml;ffentliche Mitteilung. (Nur f&uuml;r Admins)</td>
            </tr>


        </table>
        <h3>Changelog</h3>
        <ul>
            <li>
            <li>[03.02.2014] Codename: rabbit nom noms carrots
                <ul>
                    <li>github eingecheckt <a href="https://github.com/xidus/mensierbot" target="_blank">https://github.com/xidus/mensierbot</a></li>
                    <li>Zeitmethode ausgelagert. Bot ist etwas toleranter bei Eingaben</li>
                    <li>Userdatenbank mit Admins und reminder erstellt</li>
                    <li>Website etwas poliert und den Bug behoben, das auf der Website das Datum nicht mehr angezeigt worden ist </li>
                </ul>
            </li>

        </li>

        <li>[29.01.2014] Codename: rabbit nom nom nom
            <ul>
                <li>psa Befehl</li>
                <li>Zeiten werden jetzt auch ohne Doppelpunkt akzeptiert (Gr&uuml;&szlig;e an Jonas)</li>
                <li>Die Website wurde aus den tweets entfernt um die Lesbarkeit zu verbessern</li>
                <li>Die Sortierung der Tweets und Websiteanzeige ist nun nach der Uhrzeit</li>
                <li>Die Website wurde &uuml;berarbeitet</li>
                <li>Der Bot ignoriert nun gross und klein Schreibung</li>
            </ul>
        </li>
    </ul>

    <h3>Known Bugs / ToDo / Wishlist</h3>
    <ul>
        <li>Reminder</li>
        <li>Website polieren</li>
        <li>Mensaw&uuml;nsche per Web mitteilen</li>
        <li>???</li>
        <li>Weltherrschaft!</li>
    </ul>

    Der Bot befindet sich aktuell in einer sehr beta Phase. Vorschl&auml;ge, Kritik und Bugs bitte per twitter an @powerdan. Lust auf Bughunting? Schau in den <a href="bot.log">Botlog</a>
</body>
</html>