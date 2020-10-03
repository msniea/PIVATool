
package soot.codeanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import soot.jimple.infoflow.android.data.AndroidMethod;
import soot.jimple.infoflow.android.data.AndroidMethod.CATEGORY;
import soot.jimple.infoflow.android.data.AndroidMethodCategoryComparator;
import soot.jimple.infoflow.sourcesSinks.definitions.MethodSourceSinkDefinition;
import soot.jimple.infoflow.sourcesSinks.definitions.SourceSinkDefinition;
import soot.jimple.infoflow.sourcesSinks.definitions.SourceSinkType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JLookupSwitchStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.ide.icfg.AbstractJimpleBasedICFG;
import soot.Body;
import soot.Local;
import soot.Type;
import soot.Unit;
import soot.BodyTransformer;
import soot.G;
import soot.PackManager;
import soot.PhaseOptions;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SourceLocator;
import soot.Transform;
import soot.UnitBox;
import soot.UnitPatchingChain;
import soot.UnitPrinter;
import soot.Value;
import soot.ValueBox;
import soot.jbco.jimpleTransformations.GotoInstrumenter;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.options.Options;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalBlockGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;

import java.util.Arrays;
import java.util.Stack;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;

import fj.P;
import fj.test.Bool;
import java_cup.shift_action;
import polyglot.ast.Branch;
import afu.org.checkerframework.checker.units.qual.m;
import android.Manifest;
import android.R.integer;
import soot.toolkits.graph.UnitGraph;
import soot.jimple.IfStmt;
import soot.toolkits.scalar.UnitValueBoxPair;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.tools.CFGViewer;
import soot.util.Chain;
import soot.util.Switch;
import soot.util.cfgcmd.CFGGraphType;

public class PIVATool{
	public static ArrayList<String> classlist = new ArrayList<String>();
	public static List<String> ComponentList=new ArrayList<String>();
	public static List<Block> BlockList=new ArrayList<Block>();
	public static Boolean MultipleIntent=false;
	public static Boolean ApkHasVulnerability=false;
	public static Iterator methodIt;
	public static Chain<Local> LocalList;
	public static long StartTime;
	public static long EndTime;
	public static long TotalTime;
	public static long StartTimeJimple;
	public static long EndTimeJimple;
	public static long TotalTimeJimple;

	public static void main(String[] args) {
		StartTimeJimple = System.currentTimeMillis();

		String ManifestPath="C:/soot project/apk/Notepad_1.82/AndroidManifest(Notepad_1.82).xml";
		ManifestAnalyser Analyser=new ManifestAnalyser(ManifestPath);
		ComponentList=Analyser.getComponents();
		System.out.println( "****     PIVATool     ****");
		System.out.println( "apk Component List is : " + ComponentList);
		Options.v().set_android_jars("F:/Android-Sdk/platforms");
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_process_dir(Collections.singletonList("C:/soot project/apk/Notepad_1.82.apk"));		
		Scene.v().setSootClassPath(Scene.v().getSootClassPath() + ";"			     
				+ "C:/Program Files/Java/jdk1.8.0_74/jre/lib/jce.jar" + ";" 
				+ "F:/Android-Sdk/platforms/android-29/android.jar" + ";" 
				+ "F:/a/u/p/s/b/s/target/sootclasses-trunk-jar-with-dependencies.jar" + ";" 
				+ "C:/Program Files/Java/jdk1.8.0_74/jre/lib/rt.jar" + ";" 
				+ "F:/Android-Sdk/platforms" + ";" 			    
				+ Options.v().process_dir().get(0).toString());
		Options.v().set_keep_line_number(false);
		Options.v().set_prepend_classpath(true);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_whole_program(true);
		Options.v().set_verbose(false);
		Options.v().set_output_format(Options.output_format_jimple);
		Options.v().set_output_dir("C:/soot project/OutputOfSoot");	
		Scene.v().loadNecessaryClasses();				
		PackManager.v().writeOutput();
		DeleteAdditionalClass();
		EndTimeJimple   = System.currentTimeMillis();
		TotalTimeJimple = EndTimeJimple - StartTimeJimple;
		NumberFormat formatter = new DecimalFormat("#0.00000");
		System.out.println("Run Time for apk conversion to Jimple is " + formatter.format((EndTimeJimple - StartTimeJimple) / 1000d) + " seconds");		
		loadClass(); 
	}
	private static void DeleteAdditionalClass() {	
		File dir = new File("C:/soot project/OutputOfSoot");
		List<String> FileList = Arrays.asList(dir.list(
				new FilenameFilter() {
					@Override public boolean accept(File dir, String name) {
						return name.endsWith(".jimple");
					}
				}
				));
		for (int FileNum=0;FileNum<FileList.size();FileNum++){
			Boolean isEqual=false;
			String a=FileList.get(FileNum);
			for (int ComponentNum=0;ComponentNum<ComponentList.size();ComponentNum++){
				String b=ComponentList.get(ComponentNum);
				if (FileList.get(FileNum).contains(ComponentList.get(ComponentNum))){
					isEqual=true;
				}
			}
			if (isEqual==false ){
				File file=new File("C:/soot project/OutputOfSoot/" + FileList.get(FileNum));
				file.delete();
			}
			if (FileList.get(FileNum).contains("$")){
				File file=new File("C:/soot project/OutputOfSoot/" + FileList.get(FileNum));
				file.delete();
			}
		}	
	}	
	private static void loadClass() {	
		StartTime = System.currentTimeMillis();
		G.v().reset();
		Scene.v().setSootClassPath(Scene.v().getSootClassPath() +";C:/soot project/OutputOfSoot");
		Options.v().set_whole_program(true);
		Options.v().set_prepend_classpath(true);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_android_jars("C:/a/Android-Sdk/platforms");
		Options.v().set_src_prec(Options.src_prec_jimple);	
		for (int i=0;i<ComponentList.size();i++)
		{
			String cl=ComponentList.get(i);
			Scene.v().addBasicClass(cl,SootClass.SIGNATURES);	   
			SootClass sootclass=Scene.v().loadClassAndSupport(cl);
			Scene.v().forceResolve(cl, SootClass.BODIES);
			sootclass.setApplicationClass(); 
		}
		Scene.v().loadNecessaryClasses();		
		loadMethods();
	}
	private static void loadMethods(){		
		for (final SootClass sclass: Scene.v().getApplicationClasses()) {
			methodIt = sclass.getMethods().iterator();
			while (methodIt.hasNext()) {
				List<String> defList=new ArrayList<String>();					
				SootMethod sootmethod = (SootMethod)methodIt.next();
				String methodname=sootmethod.getName();											
				if (!(sootmethod.retrieveActiveBody().getUnits().isEmpty())){
					Body body=sootmethod.retrieveActiveBody();
					LocalList=body.getLocals();
					Iterator LocalIt=LocalList.iterator();
					while (LocalIt.hasNext()) {
						Local LocalValue= (Local)LocalIt.next();
						if (LocalValue.getType().toString().equals("android.app.PendingIntent")){
							defList.add(LocalValue.toString());
						}
					}				
					if (!defList.isEmpty()){
						loadBody(defList,body,sclass,sootmethod,methodname);
						System.out.println("------------------------------------------------------------------------------");		
					}
				}
			}					
		}
		if (ApkHasVulnerability==false)
		{
			System.out.println("This Application does not have any pending intent related vulnerabilities");
		}
		EndTime   = System.currentTimeMillis();
		TotalTime = EndTime - StartTime;
		NumberFormat formatter = new DecimalFormat("#0.00000");
		System.out.print("Run Time for apk Analysis is " + formatter.format((EndTime - StartTime) / 1000d) + " seconds");
	}
	private static void loadBody(List<String> defList,Body body,SootClass sclass,SootMethod sootmethod,String CallerMethodName)
	{
		List<StatementProperties> BodyUnits=new ArrayList<StatementProperties>();
		Boolean defIsPending=false;
		BlockGraph blockGraph = new ExceptionalBlockGraph(body);	
		BlockList=blockGraph.getBlocks();
		List<StatementProperties> StmtProList=new ArrayList<StatementProperties>();
		List<AnalysisStatement> PendingIntentList=new ArrayList<AnalysisStatement>();
		defIsPending=false;
		MultipleIntent=false;
		for (int blocknum=0;blocknum<BlockList.size();blocknum++) {
			List<StatementProperties> BlockUnits=new ArrayList<StatementProperties>();
			int UnitNumber=-1;
			Iterator<Unit> blockIt=BlockList.get(blocknum).iterator();
			while (blockIt.hasNext()) {
				UnitNumber=UnitNumber+1;									 
				Unit UnitValue= (Unit)blockIt.next();	
				StatementProperties StmtPro=new StatementProperties();
				StmtPro.unit=UnitValue;
				StmtPro.UnitNumber=UnitNumber;					
				BodyUnits.add(StmtPro);
				Stmt Statement=(Stmt) UnitValue;
				Iterator defIt=Statement.getDefBoxes().iterator();
				Iterator useIt=Statement.getUseBoxes().iterator();
				while (defIt.hasNext()) {
					ValueBox defUnit=(ValueBox)defIt.next();																			 
					Value defValue=defUnit.getValue();					
					StmtPro.Def=defValue;
					if (defList.contains(defValue.toString()) && Statement.containsInvokeExpr()){
						InvokeExpr invokeExpersion=Statement.getInvokeExpr();												 
						String invokeExprType=invokeExpersion.getType().toString();
						String invokeExprMethod=invokeExpersion.getMethod().getName().toString();
						String Context=Statement.toString();
						Context=Context.substring(Context.indexOf("<")+1);
						Context=Context.substring(0,Context.indexOf(":"));											
						if (Context.equals("android.app.PendingIntent") && (invokeExprType.equals("android.app.PendingIntent")) && (invokeExprMethod.equals("getActivity") || invokeExprMethod.equals("getService") || invokeExprMethod.equals("getBroadcast")  || invokeExprMethod.equals("getActivities"))){																													
							AnalysisStatement analysisStatement=new AnalysisStatement();
							defIsPending=true;
							analysisStatement.BlockList=BlockList;
							analysisStatement.Def=defValue.toString();
							analysisStatement.UnitNumber=UnitNumber;								
							IntentBaseProperties IntentBase=new IntentBaseProperties();
							IntentBase.IntentName=invokeExpersion.getArg(2).toString();
							IntentBase.BlockNumber=blocknum;
							analysisStatement.IntentBaseList.add(IntentBase);
							analysisStatement.IntentBase=IntentBase;
							analysisStatement.Arg=invokeExpersion.getArg(2).toString();								
							analysisStatement.Unit=UnitValue;													 
							analysisStatement.MethodName=invokeExprMethod;
							analysisStatement.Body=body;
							analysisStatement.BodyUnit=BodyUnits;
							analysisStatement.Type=1;
							analysisStatement.sClassName=sclass.toString();
							analysisStatement.sMethodName=sootmethod.getName().toString();
							analysisStatement.sMethodType=sootmethod.getReturnType().toString();
							analysisStatement.BlockNum=BlockList.get(blocknum).getIndexInMethod();
							analysisStatement.BlockUnit=BlockUnits;
							analysisStatement.Number=UnitNumber;
							analysisStatement.callerMethodName=CallerMethodName;
							if (invokeExprMethod.equals("getActivities"))
							{
								MultipleIntent=true;
							}
							PendingIntentList.add(analysisStatement);																																			
						}						
					}
				}
				while (useIt.hasNext()) {																					 
					ValueBox useUnit=(ValueBox)useIt.next();																																 
					Value useValue=useUnit.getValue();
					if(LocalList.contains(useValue)){
						StmtPro.useList.add(useValue);		
						StmtPro.UseList.add(useValue.toString());
						for (int PINumber=0;PINumber<PendingIntentList.size();PINumber++){
							AnalysisStatement analysisStmt=PendingIntentList.get(PINumber);					
							if (analysisStmt.Def.equals(useValue.toString()) || analysisStmt.DefRepList.contains(useValue.toString())){
								UseUnit use=new UseUnit();
								use.UseUnit=UnitValue;
								use.UseBlock=BlockUnits;
								use.UseNumber=UnitNumber;
								use.BlockNumber=blocknum;								
								analysisStmt.UseList.add(use);																									 						
								if ((!(StmtPro.Def==null)) && LocalList.contains(StmtPro.Def))														
								{
									analysisStmt.DefRepList.add(StmtPro.Def.toString());
								}							
							}
						}
					}					
				}
				BlockUnits.add(StmtPro);
				StmtProList.add(StmtPro);
			}	
		}
		for (int PINum=0;PINum<PendingIntentList.size();PINum++){			
			AnalysisStatement analysisStatement2=PendingIntentList.get(PINum);
			analysisStatement2.StatementPropertiesList=StmtProList;
			if (!analysisStatement2.UseList.isEmpty()){
				SinkAnalysis(analysisStatement2);
			}
		}					
	}
	private static void SinkAnalysis(AnalysisStatement analysisStatement)
	{
		for (int UseNum=0;UseNum<analysisStatement.UseList.size();UseNum++){
			UseUnit use=(UseUnit) analysisStatement.UseList.get(UseNum);
			Unit unit=use.UseUnit;		
			Stmt Statement=(Stmt) unit;    			
			if (Statement.containsInvokeExpr())
			{	
				String Context=Statement.toString();
				Context=Context.substring(Context.indexOf("<")+1);
				Context=Context.substring(0,Context.indexOf(":"));				  
				InvokeExpr invokeExpersion=Statement.getInvokeExpr();
				String invokeExprType=invokeExpersion.getType().toString();
				String invokeExprMethodName=invokeExpersion.getMethod().getName().toString();						
				if (invokeExprType.equals("android.content.Intent") && invokeExprMethodName.equals("putExtra"))
				{	
					AnalysisStatement analysisStatement2=new AnalysisStatement();
					String StatementString=Statement.toString();
					StatementString=StatementString.substring(StatementString.indexOf(" ")+1);
					StatementString=StatementString.substring(0,StatementString.indexOf("."));
					analysisStatement2.Number=use.UseNumber;
					analysisStatement2.Unit=unit;
					analysisStatement2.BlockUnit=use.UseBlock;
					analysisStatement2.PredecessorList=use.UsePredecessorList;
					analysisStatement2.Type=2;
					analysisStatement2.Arg=StatementString;
					analysisStatement2.MethodName=invokeExprMethodName;
					analysisStatement2.BlockList=analysisStatement.BlockList;
					analysisStatement2.IntentBase.IntentName=StatementString;
					IntentBaseProperties IntentBase=new IntentBaseProperties();
					IntentBase.IntentName=StatementString;
					IntentBase.BlockNumber=use.BlockNumber;
					IntentBase.Owner="putExtra";
					analysisStatement2.IntentBase.Owner="putExtra";
					analysisStatement2.IntentBaseList.add(IntentBase);
					analysisStatement2=LoadIntentBaseUnit(analysisStatement2.BlockUnit,analysisStatement2);
					for (int k=0;k<analysisStatement2.IntentBaseList.size();k++){	
						IntentBaseProperties intentBase=(IntentBaseProperties) analysisStatement2.IntentBaseList.get(k);
						if (intentBase.isImplicit==true){
							analysisStatement=LoadIntentBaseUnit(analysisStatement.BlockUnit, analysisStatement);
							VulnerabilityTypeAnalysis(analysisStatement,intentBase); 
						}					
					}
				}
				if (Context.equals("android.app.AlarmManager") || Context.equals("android.widget.RemoteViews") || Context.contains("Notification"))
				{
					for (int intentNum=0;intentNum<analysisStatement.IntentBaseList.size();intentNum++){
						IntentBaseProperties intent=(IntentBaseProperties) analysisStatement.IntentBaseList.get(intentNum);						
						if (intent.Constructor==false && analysisStatement.HaveAlias==false){
							analysisStatement=LoadIntentBaseUnit(analysisStatement.BlockUnit, analysisStatement);
						}
					}										
					IntentBaseProperties intentBase=new IntentBaseProperties();
					intentBase.Owner="SystemService";
					VulnerabilityTypeAnalysis(analysisStatement,intentBase);
				}
			}
		}		
	}
	private static AnalysisStatement LoadIntentBaseUnit(List<StatementProperties> UnitList,AnalysisStatement analysisStatement){		    
		AnalysisStatement analysisStatement2=new AnalysisStatement();
		int IntentCount=0;
		for (int UnitNum=analysisStatement.Number-1;UnitNum>=0;UnitNum--)
		{			
			StatementProperties StmtPro=UnitList.get(UnitNum);
			Stmt Statement=(Stmt) StmtPro.unit;
			String defvalue=null;
			if (!(StmtPro.Def==null)){
				defvalue=StmtPro.Def.toString();
				if (defvalue.contains("[") && MultipleIntent==true){
					String IntentBaseCount=defvalue.substring(defvalue.indexOf("[")+1,defvalue.indexOf("]"));
					defvalue=defvalue.substring(0,defvalue.indexOf("["));			
					IntentCount=Integer.parseInt(IntentBaseCount);
				}		
				if (defvalue.equals(analysisStatement.Arg) && (!Statement.containsInvokeExpr()) ){												
					Iterator useIter=StmtPro.useList.iterator();
					while (useIter.hasNext()){
						Value useval=(Value) useIter.next();
						if (LocalList.contains(useval) && (!useval.toString().equals(defvalue))){
							IntentBaseProperties IntentBaseproperties=new IntentBaseProperties();
							IntentBaseproperties.IntentName=useval.toString();
							IntentBaseproperties.BlockNumber=analysisStatement.BlockNum;
							analysisStatement.IntentBaseList.add(IntentBaseproperties);
							analysisStatement.HaveAlias=true;
						}
					}
				}	
			}
			for (int UseNum=0;UseNum<StmtPro.UseList.size();UseNum++){			
				String usevalue=StmtPro.UseList.get(UseNum);
				for (int IntentBaseNum=0;IntentBaseNum<analysisStatement.IntentBaseList.size();IntentBaseNum++)
				{
					IntentBaseProperties IntentBase=(IntentBaseProperties) analysisStatement.IntentBaseList.get(IntentBaseNum);					
					if (usevalue.equals(IntentBase.IntentName) && Statement.containsInvokeExpr())
					{										
						InvokeExpr UseInvokeExpr=Statement.getInvokeExpr();
						if (UseInvokeExpr.getMethod().getName().toString().equals("<init>") && UseInvokeExpr.getType().toString().equals("void")){
							IntentBase.Constructor=true;
							if(UseInvokeExpr.getArgs().isEmpty()==false){
								int ArgCount=UseInvokeExpr.getArgCount();
								for (int k=0;k<ArgCount;k++){
									String UseInvokeExprArgType=UseInvokeExpr.getArg(k).getType().toString();
									String UseInvokeExprArgName=UseInvokeExpr.getArg(k).toString();
									if (UseInvokeExprArgType.equals("java.lang.Class")){
										IntentBase.isExplicit=true;
										IntentBase.isImplicit=false;
									}							
									if (UseInvokeExprArgType.equals("java.lang.String"))
									{							
										IntentBase.Action=UseInvokeExprArgName;								
									}
								}
							}
							IntentCount=IntentCount-1;
							if (IntentCount<0){
								UnitNum=-1;
							}
						}
						if (UseInvokeExpr.getMethod().getName().toString().equals("setAction") || UseInvokeExpr.getMethod().getName().equals("setClassName") ||  UseInvokeExpr.getMethod().getName().equals("setClass") || UseInvokeExpr.getMethod().getName().equals("setComponent") || UseInvokeExpr.getMethod().getName().equals("setPackage") || UseInvokeExpr.getMethod().getName().equals("setSelector"))
						{							
							IntentBase.Action=UseInvokeExpr.getArg(0).toString();
						}
						if ( UseInvokeExpr.getMethod().getName().equals("setClassName") ||  UseInvokeExpr.getMethod().getName().equals("setClass") || UseInvokeExpr.getMethod().getName().equals("setComponent") || UseInvokeExpr.getMethod().getName().equals("setPackage") || UseInvokeExpr.getMethod().getName().equals("setSelector"))
						{
							IntentBase.isExplicit=true;
							IntentBase.isImplicit=false;
						}
					}	
				}
			}				
		}
		analysisStatement2=VulnerabilityAnalysis(analysisStatement);
		return analysisStatement2;
	}	
	private static AnalysisStatement VulnerabilityAnalysis(AnalysisStatement analysisStatement)
	{
		AnalysisStatement analysisStatement3=new AnalysisStatement();
		List<Block> BlockCheckList=new ArrayList<Block>();
		for (int i=0;i<analysisStatement.IntentBaseList.size();i++){
			IntentBaseProperties IntentBase=(IntentBaseProperties) analysisStatement.IntentBaseList.get(i);
			if (IntentBase.Constructor==false && analysisStatement.HaveAlias==false)
			{
				Boolean IsConstructor=false;
				Iterator PredIt=analysisStatement.BlockList.get(IntentBase.BlockNumber).getPreds().iterator();
				while(PredIt.hasNext() && IsConstructor==false){
					Block PredBlock=(Block) PredIt.next();		
					if (!BlockCheckList.contains(PredBlock)){
						analysisStatement.BlockNum=PredBlock.getIndexInMethod();	
						BlockCheckList.add(PredBlock);
						analysisStatement3=LoadIntentBaseUnitForBranches(PredBlock,analysisStatement);
						if (analysisStatement.IntentBase.Constructor==true){
							IsConstructor=true;
						}
					}
				}
				analysisStatement.HaveAlias=true;
			}
		}
		for (int IntentBaseNum=0;IntentBaseNum<analysisStatement.IntentBaseList.size();IntentBaseNum++){
			IntentBaseProperties Intentbase=(IntentBaseProperties) analysisStatement.IntentBaseList.get(IntentBaseNum);
			if (Intentbase.isExplicit==false && Intentbase.isImplicit==false && Intentbase.Constructor==true)
			{	
				Intentbase.isImplicit=true;			
			}	  		
			if (Intentbase.isExplicit==true && analysisStatement.Type==1){
/*				System.out.println("There is a pending intent in Block " + Intentbase.BlockNumber + " of  Method " + analysisStatement.sMethodName +  " in class " + analysisStatement.sClassName + " and Base Intent Type is Explicit ");		  
*/			}
			if (Intentbase.isImplicit==true){
				if (Intentbase.Action==null){
					Intentbase.VulType.VulnerabilityType=3;
				}
				else if (!(Intentbase.Action==null))
				{
					Intentbase.VulType.VulnerabilityType=2;
					ActionAnalysis(analysisStatement,Intentbase);			 
				}
				if(analysisStatement.Type==1){
/*					System.out.println("There is a pending intent in Block " + Intentbase.BlockNumber + " of Method " +  analysisStatement.callerMethodName + " in class " + analysisStatement.sClassName +  " and Base Intent Type is Implicit");
*/				}
			}
		}
		return analysisStatement;
	}
	private static AnalysisStatement LoadIntentBaseUnitForBranches(Block PredBlock,AnalysisStatement analysisStatement){		    
		AnalysisStatement analysisStatement2=new AnalysisStatement();
		int IntentCount=0;
		Iterator<Unit> blockUnitsit=PredBlock.iterator();		    								    		
		while (blockUnitsit.hasNext()) {
			Unit unit= (Unit)blockUnitsit.next();			
			Stmt Statement=(Stmt) unit;
			String defvalue=null;
			Iterator defit=unit.getDefBoxes().iterator();
			while (defit.hasNext()) {
				ValueBox defunit=(ValueBox)defit.next();
				defvalue=defunit.getValue().toString();
				if (!(defvalue==null) && defvalue.contains("[") && MultipleIntent==true){
					String IntentBaseCount=defvalue.substring(defvalue.indexOf("[")+1,defvalue.indexOf("]"));
					defvalue=defvalue.substring(0,defvalue.indexOf("["));			
					IntentCount=Integer.parseInt(IntentBaseCount);
				}
			}			
			if ((!(defvalue==null)) && defvalue.equals(analysisStatement.Arg) && (!Statement.containsInvokeExpr())){											
				Iterator useIter=unit.getUseBoxes().iterator();
				while (useIter.hasNext()){
					ValueBox usevalBox=(ValueBox) useIter.next();
					Value useval=usevalBox.getValue();
					if (LocalList.contains(useval) && (!useval.toString().equals(defvalue))){
						IntentBaseProperties IntentBaseproperties=new IntentBaseProperties();
						IntentBaseproperties.IntentName=useval.toString();
						IntentBaseproperties.BlockNumber=analysisStatement.BlockNum;
						analysisStatement.IntentBaseList.add(IntentBaseproperties);
						analysisStatement.HaveAlias=true;
					}
				}																																		
			}		
			Iterator useIt = unit.getUseBoxes().iterator();
			while (useIt.hasNext()) {
				ValueBox useunit=(ValueBox)useIt.next();								    	
				Value usevalue=useunit.getValue();
				for (int i=0;i<analysisStatement.IntentBaseList.size();i++){
					IntentBaseProperties IntentBase=(IntentBaseProperties) analysisStatement.IntentBaseList.get(i);					
					if ( usevalue.toString().equals(IntentBase.IntentName) && Statement.containsInvokeExpr())
					{
						InvokeExpr UseInvokeExpr=Statement.getInvokeExpr();
						if (UseInvokeExpr.getMethod().getName().toString().equals("<init>") && UseInvokeExpr.getType().toString().equals("void")){
							IntentBase.Constructor=true;
							if(UseInvokeExpr.getArgs().isEmpty()==false){
								int ArgCount=UseInvokeExpr.getArgCount();
								for (int k=0;k<ArgCount;k++){
									String UseInvokeExprArgType=UseInvokeExpr.getArg(k).getType().toString();
									String UseInvokeExprArgName=UseInvokeExpr.getArg(k).toString();
									if (UseInvokeExprArgType.equals("java.lang.Class")){
										IntentBase.isExplicit=true;
										IntentBase.isImplicit=false;
									}							
									if (UseInvokeExprArgType.equals("java.lang.String"))
									{							
										IntentBase.Action=UseInvokeExprArgName;								
									}
								}
							}
							if (usevalue.toString().equals(analysisStatement.IntentBase.IntentName)){
								analysisStatement.IntentBase.Constructor=true;
							}
						}
						else if (UseInvokeExpr.getMethod().getName().toString().equals("setAction") || UseInvokeExpr.getMethod().getName().equals("setClassName") ||  UseInvokeExpr.getMethod().getName().equals("setClass") || UseInvokeExpr.getMethod().getName().equals("setComponent") || UseInvokeExpr.getMethod().getName().equals("setPackage") || UseInvokeExpr.getMethod().getName().equals("setSelector"))
						{							
							IntentBase.Action=UseInvokeExpr.getArg(0).toString();
						}
						else if ( UseInvokeExpr.getMethod().getName().equals("setClassName") ||  UseInvokeExpr.getMethod().getName().equals("setClass") || UseInvokeExpr.getMethod().getName().equals("setComponent") || UseInvokeExpr.getMethod().getName().equals("setPackage") || UseInvokeExpr.getMethod().getName().equals("setSelector"))
						{
							IntentBase.isExplicit=true;
							IntentBase.isImplicit=false;
						}									
					}
				}
			}				
		}	
		return analysisStatement2;
	}
	private static void VulnerabilityTypeAnalysis(AnalysisStatement analysisStatement,IntentBaseProperties WrapperIntent)
	{
		for (int IntentBaseNum=0;IntentBaseNum<analysisStatement.IntentBaseList.size();IntentBaseNum++){
			IntentBaseProperties IntentBase=(IntentBaseProperties) analysisStatement.IntentBaseList.get(IntentBaseNum);
			if (IntentBase.isExplicit==true && WrapperIntent.Owner.equals("putExtra") && WrapperIntent.ActionIsNotStandard==true){
				IntentBase.VulType.VulnerabilityType=4;
				ApkHasVulnerability=true;
				System.out.println("This Application has Type 4 vulnerability meaning that Explicit pending Intent is wrraped in an implicit Intent with NotStandard Action in class " + analysisStatement.sClassName + " and Method " + analysisStatement.callerMethodName);
			}
			else if (IntentBase.isImplicit==true){
				if (IntentBase.VulType.VulnerabilityType==2 && WrapperIntent.Owner.equals("putExtra")){
					ApkHasVulnerability=true;
					System.out.println("This Application has Type 2 vulnerability meaning that Implicit pending Intent with Action is wrraped in an implicit Intent in class " + analysisStatement.sClassName + " and Method " + analysisStatement.callerMethodName);
				}
				else if (IntentBase.VulType.VulnerabilityType==3 && WrapperIntent.Owner.equals("putExtra")){
					ApkHasVulnerability=true;
					System.out.println("This Application has Type 3 vulnerability meaning that Implicit pending Intent with out Action is wrraped in an implicit Intent in class " + analysisStatement.sClassName+ " and Method " + analysisStatement.callerMethodName);
				}
				else if (WrapperIntent.Owner.equals("SystemService"))
				{
					ApkHasVulnerability=true;
					System.out.println("This Application has Type 1 vulnerability meaning that Implicit pending Intent is send to system service in class " + analysisStatement.sClassName+ " and Method " + analysisStatement.callerMethodName);				
				}
			}
		}
	}

	private static void ActionAnalysis(AnalysisStatement analysisStatement,IntentBaseProperties IntentBase)
	{
		ArrayList<String> ActionList = new ArrayList<String>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("F:/Android-Sdk/platforms/android-29/data/AllAction.txt"));		
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				ActionList.add(sCurrentLine);
			} 
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		Boolean IsStandard=false;
		for (int ActionNum=0;ActionNum<ActionList.size();ActionNum++)
		{
			String ActionName=ActionList.get(ActionNum);		
			String PIAction=IntentBase.Action;
			char FirstCharacter=PIAction.charAt(0);
			char LastCharacter=PIAction.charAt(PIAction.length()-1);
			String FirstChar=Character.toString(FirstCharacter);
			String LastChar=Character.toString(LastCharacter);
			PIAction=PIAction.substring(1,PIAction.length());		  
			PIAction=PIAction.substring(0,PIAction.length()-1);		  
			if (ActionName.contains(PIAction))
			{
				IsStandard=true;
				ActionNum=ActionList.size();
			}
		}
		if (IsStandard==false)
		{
			IntentBase.ActionIsNotStandard=true;			
		}
	}
}



