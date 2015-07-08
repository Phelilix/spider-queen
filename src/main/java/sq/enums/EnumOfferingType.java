package sq.enums;

/**
 * Defines the possible offerings in the game.
 */
public enum EnumOfferingType
{
	BRAIN,
	HEART,
	SKULL;
	
	public String getName()
	{
		return name().toLowerCase();
	}
}
