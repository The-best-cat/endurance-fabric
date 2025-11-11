package net.theblackcat.endurance.interfaces;

public interface IPlayerEntity {
    void OnAppliedDeepWound();
    void OnRemovedDeepWound();
    void OnEndured();

    float GetTemporaryHealth();
    void SetTemporaryHealth(float thp);
    void AddMendingProgress(float thp);
    boolean LossPaused();
    boolean HasDeepWound();
    float GetHealRate();
    int GetMendedProgress();

    int GetInjuredTime();
    int GetRemovedInjuriesTime();
}
