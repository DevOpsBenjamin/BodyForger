package app.bodyforger.core.ble.huawei

/**
 * Le matériel cryptographique d'un modèle de balance.
 *
 * ⚠️ **Ces constantes ne sont pas des propriétés de la famille Haige, mais d'un modèle.**
 * Celles connues ont été extraites de la **Scale 3 Pro** ; `TECH.md` §2.1 les présente comme
 * de simples « constantes statiques », sans jamais dire pour quel matériel elles valent — et
 * le document ne couvre que la Pro. Rien ne prouve qu'un autre modèle partage les mêmes.
 *
 * Le nom même des deux tables de boîte blanche porte un suffixe numéroté, ce qui suggère une
 * indexation par version ou par plateforme plutôt qu'un secret unique.
 *
 * Les loger sur le modèle plutôt que dans le moteur cryptographique rend la question
 * visible et **réfutable** : les autres modèles reçoivent le matériel de la Pro comme
 * hypothèse de travail, de sorte qu'un possesseur d'une autre balance puisse essayer avec
 * du code complet. Un handshake refusé sur ce modèle est alors une information, pas une
 * panne — c'est la preuve qu'il lui faut ses propres constantes.
 */
data class HuaweiKeyMaterial(
    /** Secret d'authentification, mêlé aux sels pour dériver les jetons. */
    val authenticationSecret: ByteArray,
    /** Première table de boîte blanche. */
    val whiteboxFirst: ByteArray,
    /** Seconde table de boîte blanche. */
    val whiteboxSecond: ByteArray
) {
    init {
        require(authenticationSecret.size == 16) { "Secret d'authentification de taille invalide" }
        require(whiteboxFirst.size == 16) { "Première table de taille invalide" }
        require(whiteboxSecond.size == 16) { "Seconde table de taille invalide" }
    }

    // Les tableaux imposent une égalité structurelle explicite.
    override fun equals(other: Any?): Boolean = this === other || (other is HuaweiKeyMaterial &&
        authenticationSecret.contentEquals(other.authenticationSecret) &&
        whiteboxFirst.contentEquals(other.whiteboxFirst) &&
        whiteboxSecond.contentEquals(other.whiteboxSecond))

    override fun hashCode(): Int = authenticationSecret.contentHashCode()

    companion object {
        /**
         * Le matériel relevé sur la **Scale 3 Pro** (`M00F`), et vérifié sur elle seule.
         *
         * Sert aussi de défaut aux autres modèles, à titre d'hypothèse. Attention au
         * diagnostic : une clé racine fausse échoue **silencieusement** — la balance refuse
         * sans dire pourquoi. Un refus n'est donc pas nécessairement une clé fausse.
         */
        val SCALE_3_PRO = HuaweiKeyMaterial(
            authenticationSecret = "90B96ECA297EF78717E66E491084D3F8".hexToBytes(),
            whiteboxFirst = "CA4946D061C9FE534F6044F930EBB69B".hexToBytes(),
            whiteboxSecond = "FBCE6E2B4BAF80ED969BA26B4A4B9325".hexToBytes()
        )

        private fun String.hexToBytes(): ByteArray =
            ByteArray(length / 2) { substring(it * 2, it * 2 + 2).toInt(16).toByte() }
    }
}
