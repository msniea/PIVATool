package soot.codeanalysis;

import java.util.ArrayList;
import java.util.List;

import soot.jimple.Stmt;
import soot.toolkits.graph.Block;
import soot.Body;
import soot.Unit;
import soot.Value;

public class AnalysisStatement {
	Body Body;
	String Arg;
	Unit Unit;
	int UnitNumber;
	int Type;
	int Number;
	int BlockNum;
	String Def=null;
	List<String> DefRepList=new ArrayList<String>();
	String MethodName;
	String sMethodName;
	String sMethodType;
	String callerMethodName;
	String sClassName;
	Boolean HaveAlias=false;
	Boolean MultipleIntentBase=false;
	IntentBaseProperties IntentBase=new IntentBaseProperties();
	List IntentBaseList=new ArrayList<IntentBaseProperties>();
	List PredecessorList=new ArrayList<Block>();
	List<String> AliasList=new ArrayList<String>();
	List SuccessorList=new ArrayList<Integer>();
	List UseList=new ArrayList<UseUnit>();	
	List<StatementProperties> BlockUnit=new ArrayList<StatementProperties>();
	List<Block> BlockList=new ArrayList<Block>();
	List<StatementProperties> BodyUnit=new ArrayList<StatementProperties>();
	ArrayList<StatementProperties> BackwardSlice=new ArrayList<StatementProperties>();
	ArrayList<StatementProperties> ForwardSlice=new ArrayList<StatementProperties>();
	List<StatementProperties> StatementPropertiesList=new ArrayList<StatementProperties>();

}
