# LAB3_Besturingssystemen2
Labo 3 besturingssystemen 2

##Debugger
De debugger package bevat volgende onderdelen:
- Aparte debuggerklassen voor de klassen Block, Arena, STAllocator en Allocator
- Een Main klasse die alle debuggers oproept en voor de output zorgt
- Een logger die berichten naar de console logt met een timestamp - threadId van de uitvoerende thread en een stacktrace naar waar de logmethode werd opgeroepen, door extra diepte mee te geven in de logmethode kan de stacktrace meer of minder ver weergegeven worden
- Een klasse StackTracer om de stack weer te geven in de logger
- Workerklassen om eenzelfde BLock, Arena of Allocator tegelijk te gebruiken om zo de synchronisatie te testen (niet standaard in Main verwerkt)