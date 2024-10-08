/**
 * CalculatorMain - Derivatives Calculator
 */

package view;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import model.Differentiator;
import model.ExpressionParser;
import model.Simplifier;
import structures.BinaryTree;
import structures.BinaryTreeNode;

/**
 * A console-based program to perform symbolic differentiation on mathematical expressions.
 *
 * @author Jacob Klymenko
 * @version 3.2
 */
public class CalculatorMain {

	/** The expected index position of the variable of differentiation in the input String. */
	private final static int DIFF_VAR_POS = 3;

	/** The user inputed infix notation arithmetic expression. */
	private static String myUserInput = "";

	/** Whether the user is quitting the program or not. */
	private static boolean myUserQuitOption = false;

	/** The variable of differentiation represented by a node. */
	private static BinaryTreeNode<String> myVarDiff = null;

	/** Whether user input is valid or not, depending on the number of variables in input. */
	private static boolean myInputVariableValidity = true;

	/** A private constructor to inhibit external instantiation. */
	private CalculatorMain() {
		// do nothing
	}

	/**
	 * The start point for this program.
	 *
	 * @param theArgs the command line arguments - ignored
	 */
	public static void main(final String[] theArgs) {
		System.out.println("single variable derivatives calculator... STAAARRT!!!");
		ExpressionParser.setValidFunctions();
		try (Scanner console = new Scanner(System.in)) {
			for (;;) {
				start(console);
				if (myUserQuitOption) {
					break;
				}
				if (!myInputVariableValidity) {
					System.out.println("\nplease include less than three variables in your " +
					    "input expression.");
				}
			}
		}
		System.out.println("\nthank you for trying out this calculator!");
	}

	/**
	 * Starts the first round of asking for and differentiating a symbolic expression. Both
	 * methods of parsing and differentiating are shown to the user.
	 *
	 * @param theConsole a Scanner used to gather user input
	 */
	private static void start(final Scanner theConsole) {
		final String prompt = "\nenter an expression (or \"q\" to quit): ";
		System.out.print(prompt);
		myUserInput = theConsole.nextLine();
		final BinaryTree<String> tree = getTree(theConsole, prompt);
		if (isUserQuitting(myUserInput)) {
			return;
		}
		// the user chosen variable of differentiation
		final String varDiff = myUserInput.substring(DIFF_VAR_POS, DIFF_VAR_POS + 1);
		myVarDiff = new BinaryTreeNode<String>(varDiff);
		// check to see if user exceeded variable limit
		if (!hasValidNumVars(tree.getNode(), myVarDiff.getElement())) {
			return;
		}
		// main portion
		if (myUserQuitOption || !myInputVariableValidity) {
			return;
		} else {
			try {
				final BinaryTreeNode<String> outputTreeNode =
				    Differentiator.derive(tree.getNode(), myVarDiff);
				final String derivativeStr = Differentiator.treeNodeToString(outputTreeNode, 0);
				final BinaryTreeNode<String> simplifiedExp =
				    Simplifier.simplify(outputTreeNode);
				final String simplifiedStr = Differentiator.treeNodeToString(simplifiedExp, 0);
				displayResult(myUserInput, derivativeStr, simplifiedStr);
			} catch (final Exception error) {
				System.out.println("evaluation error has occured!");
			}
		}
	}

	/**
	 * Repeatedly waits for an expression in infix notation to be entered by the user. It
	 * runs the first input and after some parsing, feeds it to the shunting yard algorithm.
	 *
	 * This method also handles misplaced parentheses by asking the user to input once more.
	 *
	 * @param theConsole	a Scanner used to gather user input
	 * @param thePrompt		the prompt to display to the user repeatedly
	 * @return the user inputed infix notation expression represented in a binary tree
	 */
	private static BinaryTree<String> getTree(final Scanner theConsole,
	    final String thePrompt) {
		BinaryTree<String> quit = null;
		if (isUserQuitting(myUserInput)) {
			return quit;
		}
		BinaryTree<String> expTree = null;
		try {
			// converting the expression string to list, not including Leibniz's notation
			ArrayList<String> infixList =
			    ExpressionParser.stringToList(myUserInput.substring(DIFF_VAR_POS + 2));
			expTree = ExpressionParser.shuntingYardTree(infixList);
			while (!ExpressionParser.getIsValid()) {
				System.out.println("not a valid arithmetic expression. \nyour input may " +
				    "contain misplaced parentheses. \n\nplease try again.");
				System.out.print(thePrompt);
				myUserInput = theConsole.nextLine();
				if (isUserQuitting(myUserInput)) {
					return quit;
				}
				infixList = ExpressionParser.stringToList(myUserInput);
				expTree = ExpressionParser.shuntingYardTree(infixList);
			}
		} catch (final Exception theError) {
			System.out.println("\nplease include Leibniz's notation and/or an expression to " +
			    "be differentiated!");
		}
		return expTree;
	}

	/**
	 * Return true if the user input is an upper or lower case 'q'. Otherwise return false.
	 *
	 * @param theUserInput the user input
	 * @return true if the user input is an upper or lower case 'q'; otherwise false
	 */
	private static boolean isUserQuitting(final String theUserInput) {
		if (myUserInput.equals("Q") || myUserInput.equals("q")) {
			myUserQuitOption = true;
			return true;
		} else {
			myUserQuitOption = false;
			return false;
		}
	}

	/**
	 * Return true if the user input contains less than three variables. Otherwise return false.
	 *
	 * @param theRoot the root node of the binary tree
	 * @param theVarDiffString the variable of differentiation represented as a String
	 * @return true if the user input contains less than three variables; otherwise false
	 */
	private static boolean hasValidNumVars(final BinaryTreeNode<String> theRoot,
	    final String theVarDiffString) {
		boolean result = false;
		final String rootElem = theRoot.getElement();
		if (ExpressionParser.isFunction(rootElem)) {
			result = hasValidNumVars(theRoot.getLeft(), theVarDiffString);
		} else if (Differentiator.isOperator(rootElem)) {
			result = hasValidNumVars(theRoot.getLeft(), theVarDiffString) ||
			    hasValidNumVars(theRoot.getRight(), theVarDiffString);
		} else {
			String allVars = Differentiator.treeNodeToString(theRoot, 0);
			allVars = allVars.replaceAll("[^a-zA-Z&&[^" + theVarDiffString + "]]", "");
			// adds to the set each character as a string
			Set<String> distinctVars = new HashSet<>();
			for (int i = 0; i < allVars.length(); i++) {
				String currString = Character.toString(allVars.charAt(i));
				if (!distinctVars.contains(currString)) {
					distinctVars.add(currString);
				}
			}
			// determines number of variables in the user input expression
			if (distinctVars.size() > 2) {
				myInputVariableValidity = false;
				result = false;
			} else {
				myInputVariableValidity = true;
				result = true;
			}
		}
		return result;
	}

	/**
	 * Displays the derivative of the user input in both a simplified and extended form.
	 *
	 * @param theUserInput				the user inputed expression to be differentiated
	 * @param theDerivative				the unsimplified derivative of the user chosen expression
	 * @param theSimplfiedDerivative	the simplified derivative of the user chosen expression
	 */
	private static void displayResult(final String theUserInput, final String theDerivative,
	    final String theSimplfiedDerivative) {
		System.out.println("\nsimplified solution:\n" + theUserInput + " = " +
		    theSimplfiedDerivative);
		System.out.println("extended solution:\n" + theUserInput + " = " + theDerivative + "\n");
	}

}
