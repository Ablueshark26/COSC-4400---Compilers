class NewObject{
	public static void main(String[] args) {
        Test a = new Test();
        Xinu.println(a.Get());
        Xinu.println(a.value);
        Xinu.println(new Test().Get());
    }
}
class Test { 
    int value;

    public int CallGet() {
        return this.GetVal();
    }

    public int GetVal() {
        return this.value;
    }
}
