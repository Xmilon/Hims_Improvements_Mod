package net.xmilon.himproveme.item.custom;

public class MasterKey extends Key {
    public MasterKey(Settings settings) {
        super(settings);
    }

    @Override
    protected boolean isMasterKey() {
        return true;
    }
}
