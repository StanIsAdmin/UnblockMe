/**
 * Classe principale ayant pour but de resoudre le probleme et d'afficher le resultat trouve
 * @author Stanislas Gueniffey
 * @author Allan Muranovic
 */

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Stack;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Escape {

	public static void main(String[] args) {

		// Effectue la lecture du fichier et l'initialisation de la racine de l'arbre
		Parking root = parseFileToParking(args[0]);

		// Verifie la possibilite d'une solution
		String impossibleMessage = root.isImpossible();
		if (impossibleMessage != "") {	//Solution impossible
			System.out.println("Situation :");
			root.print();
			System.out.println(String.format("Il n'y a pas de solution car %s", impossibleMessage));
		}
		else {							//Solution possible
			// On genere la meilleure solution
			Parking solution = solveParking(root);

			//S'il n'y a pas de solution
			if (solution==null) {
				System.out.println("Situation :");
				root.print();
				System.out.println("Il n'y a pas de solution au problème");
			}
			//Sinon, on empile sur un stack les etapes menant de la racine a la solution (feuille)
			else {
				Stack<Parking> path = new Stack<Parking>();
				while (solution != null) {
					path.push(solution);
					solution = solution.getParentParking(); //Remonte l'arbre
				}
				printSteps(path); //Affiche les déplacements menant a la solution
			}
		}
	}

	static Parking solveParking(Parking root) {
		/*
		 * Retourne la meilleure solution au problème du Parking ayant pour racine root.
		 * Cette solution est sous la forme d'une feuille de l'arbre des deplacements possibles.
		 * Si aucune solution n'est possible, renvoie null.
		 */
		ArrayList<Parking> prevParkings = new ArrayList<Parking>();
		ArrayList<Parking> nextParkings = new ArrayList<Parking>();
		ArrayList<Parking> newParkings = new ArrayList<Parking>();
		prevParkings.add(root); //Premier niveau: racine

		//Si le Goal Car est déjà libre:
		Parking alreadySolved = root.isSolved();
		if (alreadySolved != null) {
			return alreadySolved;
		}

		// Sinon, tant qu'il existe des nouvelles combinaisons de position des voitures
		while (!prevParkings.isEmpty()) {
			nextParkings = new ArrayList<Parking>();
			ListIterator<Parking> iter = prevParkings.listIterator();
			Parking currentParking;

			// On genere les noeuds du niveau suivant (nouveaux Parkings)
			while (iter.hasNext()) {
				currentParking = iter.next();
				newParkings = currentParking.getNextParkings(); //Nouveaux Parkings possibles

				// Un premier element nul signifie que la solution a ete trouvee
				if (!newParkings.isEmpty() && newParkings.get(0) == null) {
					return newParkings.get(1); // On retourne la solution
				}
				// Autrement, on conserve toutes les solutions pour aller plus loin
				else {
					nextParkings.addAll(newParkings);
				}
			}
			prevParkings = nextParkings;
		}

		return null; // Aucune solution trouvee

	}

	static void printSteps(Stack<Parking> path) {
		/*
		 * Affiche sur l'output standard la résolution du problème, à partir du Stack contenant chaque etape menant a la
		 * solution.
		 */
		Parking prevStep = path.pop();
		Move moveToStep;
		Parking thisStep;
		int thisMovedCar = -1;// Index de la voiture deplacee a cette etape-ci
		int prevMovedCar = -1;// Index de la voiture deplacee a l'etape precedente

		System.out.println("Situation initiale :");
        prevStep.print();

		System.out.println(String.format("\nUne façon de sortir du Parking en %d mouvements a été trouvée.", path.size()));

		while (!path.isEmpty()) {
			thisStep = path.pop();
			moveToStep = thisStep.getParentMove();
			thisMovedCar = moveToStep.getMovedCar();

			// Si c'est une nouvelle voiture qui est deplacee, on l'affiche
			if (prevMovedCar != thisMovedCar) {
				if (thisMovedCar == 0) {
					System.out.println(String.format("\nDéplacements voiture Goal :", thisMovedCar));
				}
				else {
					System.out.println(String.format("\nDéplacements car %d :", thisMovedCar));
				}

				prevStep.printCoordinates(thisMovedCar);
				prevMovedCar = thisMovedCar;
			}
			System.out.print(" -> ");
			thisStep.printCoordinates(thisMovedCar);
			prevStep = thisStep;
		}
		System.out.println("\n");

		System.out.println("Situation finale :");
        prevStep.print();
	}

	public static void arrangeCoord(int[] coordXY) {
		/*
		 * Range les coordonnées de sorte que le premier point soit le haut de la voiture si celle-ci est verticale, ou
		 * le point le plus à gauche si elle est horizontale.
		 */
		int aX = coordXY[0], aY = coordXY[1], bX = coordXY[2], bY = coordXY[3];
		// Si les composantes X ou Y de b sont superieures a celles de a, on les echange
		if (aX > bX || aY > bY) {
			coordXY[0] = bX;
			coordXY[1] = bY;
			coordXY[2] = aX;
			coordXY[3] = aY;
		}
	}

	static Parking parseFileToParking(String filename) {
		/*
		 * Parcourt le fichier filename et retourne le Parking qui y est decrit.
		 * Si une erreur est rencontrée, retourne null.
		 */
		String line; // Ligne en cours de lecture
		String[] parsed; // Elements séparés de la ligne
		int width, height; // Dimensions du parking
		int exitX=-1, exitY=-1; // Coordonnées de la sortie
		int carsCount; // Nombre de voitures

		int aX = 0, aY = 1, bX = 2, bY = 3; // Index des coordonnées
		int[][] carCoordinates = null; // Liste des coordonnees au format [backX, backY, frontX, frontY]

		try {
			File f = new File(filename);
			FileReader filerd = new FileReader(f);
			BufferedReader buffrd = new BufferedReader(filerd);

			try {
				// DIMENSIONS
				line = buffrd.readLine();
				parsed = line.split(" ");
				width = Integer.parseInt(parsed[1]); // Largeur du Parking
				height = Integer.parseInt(parsed[3]); // Hauteur du Parking
                exitX=width;	//Sortie par défaut (au cas ou dessin mal fait)
                exitY=2;

				// DESSIN DU PARKING ET SORTIES
				line = buffrd.readLine(); //Premiere ligne, retire char vides
				parsed = line.split("\\+");
				for (int i=1; i<=width; i++) { 		//Sortie en haut ?
					if (!parsed[i].equals("---")) { //Pas de delimiteur
						exitX = i-1;
						exitY = -1;
					}
				}
				for (int i = 0; i < height; i++) {	//Sortie sur cotes ?
					line = buffrd.readLine().replaceAll("\\s", ""); // Ligne importante, retire char vides
					if (!line.startsWith("|")) {
						exitX = -1;
						exitY = i;
					}
					else if (!line.endsWith("|")) {
						exitX = width;
						exitY = i;
					}
					if (i!=height-1) {
						line = buffrd.readLine(); //Passe la ligne
					}
				}
				line = buffrd.readLine(); //Dernière ligne, retire char vides
				parsed = line.split("\\+");
				for (int i=1; i<=width; ++i) {		//Sortie en bas ?
					if (!parsed[i].equals("---")) { //Pas de delimiteur
						exitX = i-1;
						exitY = height;
					}
				}

				// NOMBRE VOITURES GOAL (forcement 1)
				line = buffrd.readLine(); //Passe ligne inutile
				line = buffrd.readLine().replaceAll("\\s", ""); // Retire caractères vides
				parsed = line.split(":");

				carsCount = Integer.parseInt(parsed[1]);

				// NOMBRE D'AUTRES VOITURES
				line = buffrd.readLine().replaceAll("\\s", ""); // Retire caractères vides
				parsed = line.split(":");
				carsCount += Integer.parseInt(parsed[1]);

				line = buffrd.readLine(); // passe une ligne inutile

				// COORDONNEES DES VOITURES
				carCoordinates = new int[carsCount][4];

				for (int numberLine = 0; numberLine < carsCount; numberLine++) {
					line = buffrd.readLine().split(":")[1]; // Ligne contenant les coordonnees
					parsed = line.replaceAll("[\\)\\(\\s\\]\\[]", "").split(","); // Separe les coordonnees

					carCoordinates[numberLine][aX] = Integer.parseInt(parsed[aX]);
					carCoordinates[numberLine][aY] = Integer.parseInt(parsed[aY]);
					carCoordinates[numberLine][bX] = Integer.parseInt(parsed[bX]);
					carCoordinates[numberLine][bY] = Integer.parseInt(parsed[bY]);

					arrangeCoord(carCoordinates[numberLine]); // Rearrange les coordonnees (arriere puis avant)
				}

				buffrd.close();
				filerd.close();
				return new Parking(width, height, exitX, exitY, carCoordinates); // Fichier correct: OK
			}
			catch (IOException exception) {
				System.out.println("Error while reading : " + exception.getMessage());
			}
		}
		catch (FileNotFoundException exception) {
			System.out.println(String.format("File %s not found", filename));
		}

		return null; // Erreur -> Parking non genere
	}

}
