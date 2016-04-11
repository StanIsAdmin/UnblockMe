/**
 * Classe représentant un état du parking, donc un noeud de l'arbre parcouru pour résoudre le problème
 * @author Stanislas Gueniffey
 * @author Allan Muranovic
 */

import java.util.Arrays; //methodes hashSet et equals sur les tableaux
import java.util.ArrayList; //tableau de taille variable
import java.util.HashSet; //hash table


public class Parking {
	//MEMBRES STATIQUES
	static boolean[][] _spotsUsed;	//Matrice du parking: true si case occupee, false sinon
	static HashSet<Parking> _allParkings; //Hash Set des parkings deja generes

	static int _width, _height, _exitX, _exitY; //Largeur, Hauteur, Colonne puis Ligne de la sortie
	static int _exitSpotsBlocked; //Compteur des spots bloques entre la Goal Car et la sortie
	static boolean _exitInFront; //Retient si la sortie est a l'avant ou a l'arriere de la Goal Car

	static int _carsCount; //Nombre de voitures
	static boolean[] _carOrientations; //true si voiture d'index i est horizontale, false si verticale
	static int[] _carRanges; //Index de ligne (si horizontale) ou colonne (si verticale) des voitures
	static int[] _carSizes; //Tailles des voitures

	//MEMBRES PROPRES A CHAQUE INSTANCE
	int[] _carPositions; //Position de l'arriere des voitures dans leurs rangees (definit le parking)
	Move _parentMove; //Deplacement (arete) reliant cette situation a la precedent (noeud parent)


	public Parking isSolved() {
		/*
		 * Si le Parking est deja resolu (on suppose l'appel sur le Parking initial, la racine),
		 * retourne le dernier Parking sous forme d'une feuille de l'arbre des deplacements.
		 * Sinon, retourne la valeur null.
		 */

		setMatrix(); //Permet les verifications necessaires de disponibilite des positions
		Parking result = null;
		//Si la voiture devient libre sans deplacement, on genere la branche de la solution
		if (goalCarBecomesFree(null)) result = goalCarToExit();
		clearMatrix(); //Nettoie la matrice de facon efficace
		return result;
	}

	public String isImpossible() {
		/*
		 * Si le Parking n'a pas de solution de par la situation du Goal Car, retourne un String
		 * decrivant le probleme. Sinon, retourne un string vide.
		 */

		//La goal car n'est pas alignee avec la sortie, ou la sortie n'est pas sur un cote
		if (_carOrientations[0]) {
			if (_carRanges[0]!= _exitY) { return "la sortie et le Goal car ne sont pas sur la meme ligne"; }
			if (_exitX!=-1 && _exitX!=_width) { return "la sortie ne se situe pas sur un cote du parking"; }
		}
		else {
			if (_carRanges[0]!= _exitX) { return "la sortie et le Goal car ne sont pas sur la meme colonne"; }
			if (_exitY!=-1 && _exitY!=_height) { return "la sortie ne se situe pas sur un cote du parking"; }
		}
		//Il y a une voiture de meme orientation entre la sortie et la goal car
		boolean carBlocks = false;
		int carIndex=1;
		while (!carBlocks && carIndex<_carsCount) {
			//Voiture de meme orientation et de meme rangee que Goal Car
			if (_carOrientations[carIndex]==_carOrientations[0] && _carRanges[carIndex]==_carRanges[0]) {
				//Voiture entre Goal Car et sortie
				if (_exitInFront) carBlocks = _carPositions[carIndex]>_carPositions[0];
				else carBlocks = _carPositions[carIndex]<_carPositions[0];
			}
			carIndex+=1;
		}
		if (carBlocks) {
			return String.format("la sortie est bloquee par la voiture %d", carIndex-1);
		}
		return ""; //Parking possible -> pas d'erreur
	}

	public ArrayList<Parking> getNextParkings() {
		/*
		 * Retourne la liste des Parkings nouveaux (menant a une nouvelle disposition des voitures)
		 * pouvant etre obtenus a partir de this en deplacant une seule voiture d'une seule position.
		 * Si dans l'un deux, la Goal Car est libre, retourne une liste contenant le flag null suivi
		 * du dernier Parking de la solution (comme feuille de l'arbre).
		 * Si aucun Parking n'est nouveau, retourne une liste vide.
		 */

		setMatrix(); //Initialise la matrice permettant de verifier les places disponibles
		ArrayList<Parking> nextParkings = new ArrayList<Parking>(); //Parkings suivants
		Parking newParking; //Parking suivant courant

		//On essaie de deplacer chaque voiture vers l'avant et vers l'arriere
		for (int carIndex=0; carIndex < _carsCount; carIndex++) {

			//Essaie d'AVANCER (de 1) la voiture (si la position devant est libre)
			if (spotIsFree(getFwdSpot(carIndex, 1))) {

				int[] newCarPositions = _carPositions.clone();	//Copie des positions actuelles
				newCarPositions[carIndex] += 1; 				//Voiture carIndex avance d'une position
				newParking = new Parking(newCarPositions);	//Parking des nouvelles positions
				new Move(this, newParking, carIndex);		//Mouvement menant a ce parking

				//Si en liberant la position arriere on libere la Goal Car
				if (goalCarBecomesFree(getBwdSpot(carIndex, 0))) {
					nextParkings.clear(); //Plus besoin des autres Parkings
					nextParkings.add(null);	//Flag pour solution trouvee
					nextParkings.add(newParking.goalCarToExit()); //Dernier noeud de la solution
					return nextParkings;
				}
				//Sinon, si cette disposition de Parking est nouvelle (retour de add)
				if (_allParkings.add(newParking)) {
					nextParkings.add(newParking);
				}
			}

			//Essaie de RECULER (de 1) la voiture (si la position derriere est libre)
			if (spotIsFree(getBwdSpot(carIndex, 1))) {
				int[] newCarPositions = _carPositions.clone();	//Copie des positions actuelles
				newCarPositions[carIndex] -= 1; 				//Voiture carIndex avance d'une position
				newParking = new Parking(newCarPositions);	//Parking des nouvelles positions
				new Move(this, newParking, carIndex);		//Mouvement menant a ce parking

				//Si en liberant la position avant on libere la Goal Car
				if (goalCarBecomesFree(getFwdSpot(carIndex, 0))) {
					nextParkings.clear(); //Plus besoin des autres Parkings
					nextParkings.add(null); //Flag pour solution trouvee
					nextParkings.add(newParking.goalCarToExit()); //Dernier noeud de la solution
					return nextParkings;
				}
				//Sinon, si cette disposition de Parking est nouvelle (retour de add)
				if (_allParkings.add(newParking)) {
					nextParkings.add(newParking);
				}
			}
		}
		clearMatrix(); //Nettoie la matrice de facon efficace
		return nextParkings;
	}


	private boolean goalCarBecomesFree(int[] freedSpot) {
		/*
		 * Retourne true si en liberant la position freedSpot, on libere le Goal Car, false sinon.
		 */
		if (freedSpot==null)
			return _exitSpotsBlocked==0; //Aucune position liberee, Goal Car deja libre
		else
			return spotBlocksExit(freedSpot) && _exitSpotsBlocked==1; //freedSpot = seule position bloquante
	}

	private Parking goalCarToExit() {
		/*
		 * Prend pour acquis que le Goal Car est libre et genere la suite de la branche partant du parking
		 * actuel et menant a la solution, puis retourne la feuille obtenue.
		 */

		int[] newCarPositions=_carPositions.clone(); //Nouvelles positions des voitures
		int maxGoalPosition; //Position max. de la Goal Car (position a la sortie)
		int move; //Deplacements de la Goal Car

		Parking prevParking = this; //Parking precedent
		Parking nextParking;		//Parking suivant

		if (_exitInFront) {	//Sortie vers l'avant
			if (_carOrientations[0]) maxGoalPosition = _exitX-_carSizes[0]-1;//Selon largeur
			else maxGoalPosition = _exitY-_carSizes[0]-1;//Selon hauteur
			move = 1; //Avancer
		}
		else {				//Sortie vers l'arriere
			maxGoalPosition=0;
			move = -1; //Reculer
		}
		//Tant que Goal Car n'est pas devant sortie
		while (newCarPositions[0]!=maxGoalPosition) {
			newCarPositions = prevParking._carPositions.clone(); //Copie des positions actuelles
			newCarPositions[0] += move; //Voiture Goal se deplace d'une position
			nextParking = new Parking(newCarPositions);//Parking suivant
			new Move(prevParking, nextParking, 0);//Mouvement reliant les Parkings
			prevParking = nextParking;
		}

		return prevParking;
	}

	private int[] getBwdSpot(int carIndex, int offset) {
		/*
		 * Retourne la position a une distance de offset de l'arriere de la voiture carIndex.
		 */
		int[] spot = new int[2];
		if (_carOrientations[carIndex]) {	//Voiture horizontale
			spot[0] = _carPositions[carIndex]-offset;	//Sa composante X (col) - offset
			spot[1] = _carRanges[carIndex];				//Sa composante Y (ligne) est fixe
		}
		else {								//Voiture verticale
			spot[0] = _carRanges[carIndex];				//Sa composante X (col) est fixe
			spot[1] = _carPositions[carIndex]-offset;	//Sa composante Y (ligne) - offset
		}
		return spot;
	}

	private int[] getFwdSpot(int carIndex, int offset) {
		/*
		 * Retourne la position a une distance de offset de l'avant de la voiture carIndex.
		 */
		return getBwdSpot(carIndex, -offset-_carSizes[carIndex]);
	}

	private int[] getUsedSpot(int carIndex, int offset) {
		/*
		 * Retourne la offset-eme position occupee par la voiture carIndex.
		 */
		return getBwdSpot(carIndex, -offset);
	}

	public boolean spotIsFree(int[] spot) {
		/*
		 * Retourne true si les coordonnees de la position spot sont correctes et non occupees, false sinon.
		 */
		return spot[0]>=0 && spot[1]>=0 && spot[0]<_width && spot[1]<_height && !_spotsUsed[spot[1]][spot[0]];
	}

	public boolean spotBlocksExit(int[] spot) {
		/*
		 * Retourne true si les coordonnees de la position spot sont situees entre le Goal Car et la sortie,
		 * false sinon.
		 */
		boolean result = false;
		if (_carOrientations[0]) {	//Goal Car Horizontale
			if (spot[1]==_exitY) { 		//Spot sur meme ligne
				if (_exitInFront) {			//Sortie devant
					result = spot[0]>_carPositions[0]+_carSizes[0];//Spot devant Goal Car
				}
				else {						//Sortie derriere
					result = spot[0]<_carPositions[0];//Spot derriere Goal Car
				}
			}
		}
		else {						//Goal Car Verticale
			if (spot[0]==_exitX) {		//Spot Sur meme colonne
				if (_exitInFront) {			//Sortie devant
					result = spot[1]>_carPositions[0]+_carSizes[0];//Spot devant Goal Car
				}
				else {						//Sortie derriere
					result = spot[1]<_carPositions[0];//Spot derriere Goal Car
				}
			}
		}
		return result;
	}


	public boolean equals(Object o) {
		/*
		 * Redefinit la methode d'egalite pour permettre le hashing de l'objet Parking.
		 * Deux parkings sont egaux si les positions de leurs voitures sont identiques.
		 */
		if (o instanceof Parking) { //Si l'objet est bien un Parking
			Parking other = (Parking) o;	//Cast
			return Arrays.equals(_carPositions, other._carPositions); //Array.equals compare valeurs
		}
		return false;
	}

	public int hashCode() {
		/*
		 * Redefinit la methode hashCode pour permettre le hashing de l'objet Parking.
		 * Les donnees utilisees pour identifier un Parking sont les positions de ses voitures.
		 */
		return Arrays.hashCode(_carPositions); //Array.hashCode se base sur contenu du tableau
	}

	private void setMatrix() {
		/*
		 * Ecrit true dans les cases de la matrice correspondant a des positions occuppees du Parking.
		 * Calcule le nombre de voitures se situant entre le Goal Car et la sortie.
		 */
		for (int i=0; i<_carsCount; i++) {
			for (int offset=0; offset<=_carSizes[i]; offset++) {
				int[] spot = getUsedSpot(i, offset);
				_spotsUsed[spot[1]][spot[0]] = true; //Occupe les places
				if (spotBlocksExit(spot)) _exitSpotsBlocked+=1;
			}
		}
	}

	private void clearMatrix() {
		/*
		 * Ecrit false dans les cases de la matrice correspondant a des positions occuppees du Parking.
		 * Assigne 0 au compteur de voitures se situant entre le Goal Car et la sortie.
		 */
		for (int i=0; i<_carsCount; i++) {
			for (int offset=0; offset<=_carSizes[i]; offset++) {
				int[] spot = getUsedSpot(i, offset);
				_spotsUsed[spot[1]][spot[0]] = false; //Libere les places
			}
		}
		_exitSpotsBlocked=0;
	}

	public Parking getParentParking() {
		/*
		 * Retourne le noeud parent, c'est a dire le Parking parent de this dans l'arbre.
		 * Si this est la racine, retourne null.
		 */
		if (_parentMove!=null) {
			return _parentMove.getParentParking();
		}
		else {
			return null;
		}
	}

	public Move getParentMove() {
		/*
		 * Retourne l'arete de l'arbre reliant this a son noeud parent.
		 * Si this est la racine, retourne null.
		 */
		return _parentMove;
	}

	public void setParentMove(Move parentMove) {
		/*
		 * Attribue au noeud this l'arete parentMove, le reliant a son noeud parent.
		 */
		_parentMove = parentMove;
	}

	public void printCoordinates(int carIndex) {
		/*
		 * Imprime les coordonnees de la voiture carIndex.
		 */
		int[] back = getBwdSpot(carIndex, 0);
		int[] front = getFwdSpot(carIndex, 0);
		System.out.print(String.format("[(%d,%d), (%d,%d)]", back[1], back[0], front[1], front[0]));
	}

	public void print() {
		/*
		 * Imprime l'état du Parking.
		 */
		//Noms voitures a afficher
		String[][] carNames = new String[_height][_width]; //Matrice des noms (par position)
		for (int carIndex=0; carIndex<_carsCount; carIndex++) {
			for (int offset=0; offset<=_carSizes[carIndex]; offset++) {
				int[] spot = getUsedSpot(carIndex, offset);
				if (carIndex==0) {
					carNames[spot[1]][spot[0]] = " G "; //Voiture GOAL
				}
				else {
					String name = String.format(" c%d", carIndex); //Nom voiture
					carNames[spot[1]][spot[0]] = name.substring(name.length()-3);
				}
			}
		}
		//Ligne superieure
		for (int i=0; i<_width; i++) {
			if (_exitY==-1 && i==_exitX) System.out.print("+   "); //Sortie
			else System.out.print("+---"); //Mur
		}
		System.out.print("+\n");

		//Interieur du dessin
		String lineDelim = new String(new char[_width]).replace("\0", "+   ") + "+";
		for (int i=0; i<_height; i++) {

			//Noms des voitures
			if (_exitY==i && _exitX==-1) System.out.print(" ");//Sortie
			else System.out.print("|"); //Mur

			System.out.print(String.join(" ", carNames[i]).replace("null",  "   "));

			if (_exitY==i && _exitX==_width) System.out.print(" \n");//Sortie
			else System.out.print("|\n"); //Mur

			//Separateur
			if (i!=_height-1) System.out.println(lineDelim);
		}

		for (int i=0; i<_width; i++) {
			if (_exitY==_height && i==_exitX) System.out.print("+   "); //Sortie
			else System.out.print("+---"); //Mur
		}
		System.out.print("+\n");
	}

	//CONSTRUCTEUR DE NOEUD
	public Parking(int[] carPositions) {
		/*
		 * Construit un noeud de l'arbre des Parkings, sans initialiser l'arete le reliant au parent.
		 */
		_carPositions = carPositions;
		_parentMove = null; //Sera attribue a la creation de l'objet Move correspondant
	}

	//CONSTRUCTEUR INITIAL (RACINE)
	public Parking(int width, int height, int exitX, int exitY, int[][] carCoordinates) {
		/*
		 * Construit la racine de l'arbre des Parkings, et initialise ses attributs statiques.
		 */

		_width = width; //Largeur et hauteur
		_height = height;
		_exitX = exitX; //Coordonnees de la sortie (adjacente mais hors du parking)
		_exitY = exitY;
		_carsCount = carCoordinates.length; //Nb. de voitures
		_parentMove = null; //Ne sera pas modifie pour racine

		_spotsUsed = new boolean[height][width];//Initialisee avec false
		_carOrientations = new boolean[_carsCount];
		_carRanges = new int[_carsCount];
		_carSizes = new int[_carsCount];
		_carPositions = new int[_carsCount];

		//Initialise tableaux statiques et tableau des positions
		for (int carIndex=0; carIndex<_carsCount; carIndex++) {
			int[] coords = carCoordinates[carIndex]; //Coordonnees au format [backY, backX, frontY, frontX]

			if (coords[0]==coords[2]) {	//backY==frontY donc Voiture...
				_carOrientations[carIndex] = true;		//Horizontale
				_carRanges[carIndex] = coords[0];		//Appartient a ligne backY
				_carPositions[carIndex] = coords[1];	//Sur la position backX (a partir de la gauche)
				_carSizes[carIndex] = coords[3] - coords[1]; //De taille frontX-backX
			}
			else {						//Voiture...
				_carOrientations[carIndex] = false;		//Verticale
				_carRanges[carIndex] = coords[1];		//Appartient a colonne backX
				_carPositions[carIndex] = coords[0];	//Sur la position backY (a partir d'en haut)
				_carSizes[carIndex] = coords[2] - coords[0]; //De taille frontY - backY
			}
		}

		//Position de la sortie vis a vis du Goal Car
		if (_carOrientations[0]) {	//Si le Goal car est horizontal
			_exitInFront = exitX!=-1; //La sortie est devant si elle n'est pas en debut de ligne
		}
		else {						//Si le Goal car est vertical
			_exitInFront = exitY!=-1; //La sortie est devant si elle n'est pas en haut de colonne
		}

		_allParkings = new HashSet<Parking>();
		_allParkings.add(this); //Premier Parking existant
	}

}
