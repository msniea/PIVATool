package soot.codeanalysis;

import java.util.ArrayList;
import java.util.List;

import soot.Unit;
import soot.jimple.InvokeExpr;

public class IntentBaseProperties {
	Boolean Constructor=false;
	Boolean isExplicit=false;
	Boolean isImplicit=false;
	Boolean ActionIsNotStandard=false;
//	Boolean HaveReplace=false;
	Boolean CheckPreds=false;
	String Action=null;
	String IntentName=null;
	List MethodCallList=new ArrayList<InvokeExpr>();
	List IntentBaseUnit=new ArrayList<StatementProperties>();
	int BlockNumber;
	Vulnerability VulType=new Vulnerability();
	String Owner=null;
}
