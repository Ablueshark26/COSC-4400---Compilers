package Symbol;

import java.util.*;

public class Table<V> 
{
	//stack of bindings innermost on the top
	private final Map<String, Deque<V>> bindings = new HashMap<>();
	
	// One list of names per open scope
	private final Deque<List<String>> scopes = new ArrayDeque<>();

	public Table()
	{
		//global scope
		scopes.push(new ArrayList<>());
	}

	//Binds name to a value in the current scope
	public void put(String name, V value){
		bindings.computeIfAbsent(name, k -> new ArrayDeque<>()).push(value);
		scopes.peek().add(name);
	}
	
	//gets innermost binding for name and null if unbound.
	public V get(String name){
		Deque<V> stack = bindings.get(name);
		return (stack == null || stack.isEmpty()) ? null : stack.peek();
	}

	public boolean inCurrentScope(String name){
		return scopes.peek().contains(name);
	}

	public void beginScope(){
		scopes.push(new ArrayList<>());
	}
	
	public void endScope(){
		if (scopes.size() <= 1){
			throw new IllegalStateExcrption("endScope with no matching beginScope");
		}
		for (String name : scopes.pop()) {
			Deque<V> stack = bindings.get(name);
			stack.pop();
			if (stack.isEmpty()) bindings.remove(name);
		} 
	}

}
