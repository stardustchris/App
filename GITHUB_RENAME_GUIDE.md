# Guide de Renommage du Dépôt GitHub

Ce guide vous aide à renommer votre dépôt GitHub de **"App"** à **"Endurance-Flow"** (ou **"EnduranceFlow"**).

## ⚠️ Important à Savoir

- **Le renommage d'un dépôt GitHub ne peut être fait que via l'interface web GitHub** (pas par Git)
- GitHub crée **automatiquement une redirection** de l'ancien nom vers le nouveau
- Les clones locaux existants continueront de fonctionner grâce à la redirection
- **Il est recommandé de mettre à jour l'URL remote** après le renommage

---

## 🔄 Étape 1 : Renommer sur GitHub (Interface Web)

### Option A : Via les Paramètres du Dépôt

1. **Connectez-vous à GitHub** : https://github.com

2. **Accédez à votre dépôt** :
   ```
   https://github.com/stardustchris/App
   ```

3. **Ouvrez les Settings** :
   - Cliquez sur l'onglet **"Settings"** (en haut à droite du dépôt)
   - Vous devez être le propriétaire ou avoir les droits admin

4. **Section General** :
   - Descendez jusqu'à la section **"Repository name"**
   - Vous verrez le nom actuel : `App`

5. **Renommez le dépôt** :
   - Entrez le nouveau nom : **`Endurance-Flow`** ou **`EnduranceFlow`**
   - Cliquez sur **"Rename"**

6. **Confirmation** :
   - GitHub vous demandera de confirmer
   - Tapez le nouveau nom pour valider
   - Cliquez sur **"I understand, rename repository"**

---

### Option B : Via l'API GitHub (si vous préférez)

Si vous avez un token GitHub avec le scope `repo`, vous pouvez utiliser cette commande :

```bash
# Remplacez YOUR_GITHUB_TOKEN par votre token personnel
curl -X PATCH \
  -H "Authorization: token YOUR_GITHUB_TOKEN" \
  -H "Accept: application/vnd.github.v3+json" \
  https://api.github.com/repos/stardustchris/App \
  -d '{"name":"Endurance-Flow"}'
```

**Comment obtenir un token** :
1. GitHub > Settings > Developer settings > Personal access tokens
2. Generate new token (classic)
3. Cochez `repo` scope
4. Générez et copiez le token

---

## 🔄 Étape 2 : Mettre à Jour votre Dépôt Local

Après avoir renommé sur GitHub, mettez à jour l'URL remote dans votre dépôt local :

### 2.1 Vérifier l'URL actuelle

```bash
cd /home/user/App
git remote -v
```

**Résultat attendu (avant modification)** :
```
origin  http://127.0.0.1:XXXXX/git/stardustchris/App (fetch)
origin  http://127.0.0.1:XXXXX/git/stardustchris/App (push)
```

### 2.2 Mettre à jour l'URL remote

```bash
# Remplacez l'URL actuelle par la nouvelle
git remote set-url origin http://127.0.0.1:XXXXX/git/stardustchris/Endurance-Flow

# Vérifiez que c'est bien changé
git remote -v
```

**Résultat attendu (après modification)** :
```
origin  http://127.0.0.1:XXXXX/git/stardustchris/Endurance-Flow (fetch)
origin  http://127.0.0.1:XXXXX/git/stardustchris/Endurance-Flow (push)
```

### 2.3 Testez la connexion

```bash
# Vérifiez que le push fonctionne
git push origin claude/setup-app-startup-auth-QW9JS
```

**Si succès** : Tout est configuré correctement ! ✅

---

## 🔄 Étape 3 : Renommer le Dossier Local (Optionnel)

Si vous voulez également renommer le dossier local `/home/user/App` en `/home/user/Endurance-Flow` :

```bash
# Remontez d'un niveau
cd /home/user

# Renommez le dossier
mv App "Endurance-Flow"

# Entrez dans le nouveau dossier
cd "Endurance-Flow"

# Vérifiez que Git fonctionne toujours
git status
```

**Note** : Utilisez des guillemets si le nom contient un espace (`"Endurance Flow"`).

---

## 📝 Étape 4 : Mettre à Jour les Références (Si Applicable)

### Documentation à mettre à jour

Si vous avez des liens vers le dépôt dans votre documentation :

- README.md
- CONTRIBUTING.md
- Issues templates
- Pull request templates
- CI/CD configs (GitHub Actions, etc.)

**Exemple de remplacement** :

```markdown
<!-- Avant -->
https://github.com/stardustchris/App

<!-- Après -->
https://github.com/stardustchris/Endurance-Flow
```

### Clone URLs à partager

Si vous partagez l'URL de clone avec d'autres développeurs, donnez la nouvelle URL :

```bash
# Ancienne URL (fonctionnera toujours grâce à la redirection GitHub)
git clone https://github.com/stardustchris/App.git

# Nouvelle URL (recommandée)
git clone https://github.com/stardustchris/Endurance-Flow.git
```

---

## ✅ Vérification Post-Renommage

Checklist pour confirmer que tout fonctionne :

- [ ] Le dépôt apparaît avec le nouveau nom sur GitHub
- [ ] L'ancienne URL redirige vers la nouvelle (testez dans votre navigateur)
- [ ] `git remote -v` montre la nouvelle URL
- [ ] `git push` fonctionne sans erreur
- [ ] `git pull` fonctionne sans erreur
- [ ] Les liens dans la documentation sont mis à jour
- [ ] Les collaborateurs sont informés du changement

---

## 🔍 Conventions de Nommage GitHub

Voici les options recommandées pour le nouveau nom :

| Option | Avantages | Inconvénients |
|--------|-----------|---------------|
| **Endurance-Flow** | Lisible, standard GitHub (kebab-case) | Tiret peut être oublié |
| **EnduranceFlow** | Simple, pas de séparateur (PascalCase) | Moins lisible pour les longs noms |
| **endurance-flow** | Minuscules (kebab-case standard) | Perd la capitalisation |
| **endurance_flow** | Snake_case (style Python) | Moins courant sur GitHub |

**Recommandation** : **`Endurance-Flow`** (kebab-case avec capitales) pour respecter les standards GitHub tout en restant lisible.

---

## ⚠️ Problèmes Courants

### Erreur : "The repository name is already taken"

**Cause** : Un autre dépôt avec ce nom existe déjà dans votre compte.

**Solution** : Choisissez un nom différent ou supprimez l'ancien dépôt s'il n'est plus utilisé.

---

### Erreur : "You don't have permission to rename this repository"

**Cause** : Vous n'êtes pas le propriétaire du dépôt.

**Solution** : Contactez le propriétaire ou demandez les droits admin.

---

### Les commits ne se pushent plus après le renommage

**Cause** : L'URL remote n'a pas été mise à jour.

**Solution** :
```bash
git remote set-url origin https://github.com/stardustchris/Endurance-Flow.git
git push
```

---

## 📚 Ressources

- **Documentation GitHub** : https://docs.github.com/en/repositories/creating-and-managing-repositories/renaming-a-repository
- **GitHub API** : https://docs.github.com/en/rest/repos/repos#update-a-repository
- **Best Practices** : https://github.com/naming-convention

---

## 🚀 Résumé Rapide

**En 3 étapes** :

1. **GitHub Web** : Settings > Repository name > `Endurance-Flow` > Rename
2. **Local** : `git remote set-url origin <nouvelle-URL>`
3. **Vérification** : `git push` pour confirmer

**Durée totale** : ~2 minutes ⏱️

---

**Une fois le renommage effectué, informez-moi et je pourrai mettre à jour les références dans la documentation si nécessaire ! 🎉**
