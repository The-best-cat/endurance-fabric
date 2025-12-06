package net.theblackcat.endurance.interfaces;

public interface IPlayerEntity {
    void onAppliedDeepWound();
    void onRemovedDeepWound();

    float getBP();
    void setBP(float thp);
    void addMP(float thp);
    boolean lossPaused();
    boolean hasDeepWound();
    float getHealRate();
    int getMP();

    int getInjuredTime();
    int getRemovedInjuriesTime();
}
