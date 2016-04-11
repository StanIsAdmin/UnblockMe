/**
 * Classe représentant le déplacement d'une voiture, donc une arête de l'arbre parcouru pour résoudre le problème
 * @author Stanislas Gueniffey
 * @author Allan Muranovic
 */

public class Move {
	int _carIndex; //Voiture deplacee lors du deplacement
	Parking _parentParking; //Parking a partir duquel le deplacement a ete effectue
	
	public Move(Parking parentParking, Parking childParking, int carIndex) {
		/*
		 * Construit l'arete reliant le noeud parentParking a son enfant childParking, 
		 * representant de deplacement de la voiture carIndex.
		 */
		_parentParking = parentParking;
		_carIndex = carIndex;
		childParking.setParentMove(this);
	}
	
	public int getMovedCar() {
		/*
		 * Retourne la voiture concernee par le deplacement.
		 */
		return _carIndex;
	}
	
	public Parking getParentParking() {
		/*
		 * Retourne le Parking a partir duquel le deplacement a ete effectue (noeud parent).
		 */
		return _parentParking;
	}
}
