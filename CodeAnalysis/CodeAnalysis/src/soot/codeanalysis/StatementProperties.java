package soot.codeanalysis;

import java.util.ArrayList;
import java.util.List;

import soot.Unit;
import soot.Value;

public class StatementProperties {
	Unit unit;
	List<Value> defList=new ArrayList<Value>();
	List<Value> useList=new ArrayList<Value>();
	Value Def=null;
	List<String> DefList=new ArrayList<String>();
	List<String> UseList=new ArrayList<String>();
	List<String> ReleventList=new ArrayList<String>();
	int UnitNumber;
	List<Branchstatement> ControlList=new ArrayList<Branchstatement>();
	

}
