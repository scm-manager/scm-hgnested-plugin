---
title: Benutzung
---

Für die Verwaltung von Subrepositories ist eine Konfiguration erforderlich. Die zu konfigurierenden Aliasse der Subrepositories werden in den Repository-Einstellungen des übergeordneten Repositorys angelegt. 
Dazu befindet sich auf der `Repositories`-Seite in der rechten Menü-Leiste unter `Einstellungen` der Unterpunkt `Subrepository-Konfiguration`.
Die Konfiguration erlaubt es, für jedes Subrepository einen Alias zu setzen.
Dazu muss der Pfad des Subrepositorys, welcher in Bezug auf das übergeordnete Repository angegeben wird, mit dem eigentlichen Pfad des Subrepositorys verknüpft werden.
Dies kann über die Angabe des Namespaces und Namens des Repositorys im SCM-Manager geschehen oder einfach die Angabe des Namens des Repositorys sein.
Wird nur der Name angegeben, wird der Namespace des Repositorys verwendet, in dem die Einstellungen vorgenommen werden.
Es kann stattdessen auch eine (externe) URL verwendet werden.
Dieses konfigurierte Alias wird dann für Mercurial-Operationen (hg) wie pull oder push verwendet.

![Subrepository Konfiguration](assets/subrepositoryConfiguration.png)

### Berechtigungen

Jeder, der die Berechtigung hat, das Repository zu lesen, hat auch das Recht, die Konfiguration des Repositorys einzusehen. 
Die Berechtigung `Mercurial-Subrepositories konfigurieren` ist erforderlich, um die Konfiguration von Subrepositories zu erstellen und zu bearbeiten.
