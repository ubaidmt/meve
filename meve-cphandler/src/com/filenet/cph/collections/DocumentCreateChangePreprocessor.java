package com.filenet.cph.collections;

import java.util.Date;
import java.text.SimpleDateFormat;

import com.filenet.api.action.Create;
import com.filenet.api.action.PendingAction;
import com.filenet.api.core.IndependentlyPersistableObject;
import com.filenet.api.engine.ChangePreprocessor;
import com.filenet.api.exception.EngineRuntimeException;

public class DocumentCreateChangePreprocessor implements ChangePreprocessor {
	
	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private final static String EXPIRY_DATE = "2014-10-04"; // ACE Group (Desarrollo)
	
	public boolean preprocessObjectChange(IndependentlyPersistableObject sourceObj) throws EngineRuntimeException 
	{
		boolean isCreate = false;
		
		try 
		{
			// Check for various pending actions, could be helpful in determining what handler should do...
			PendingAction[] actions = sourceObj.getPendingActions();
			if (actions != null)
			{
				for (int i = 0; i < actions.length; i++)
				{
					isCreate = actions[i] instanceof Create;
					if (isCreate) 
						break;
				}
			}
		} 
		catch (Exception e) 
		{
			System.out.println("Error while trying to get pending actions from DocumentCreateChangePreprocessor. " + e.getMessage());
			return true;
		}
			
		if (isCreate) 
		{
			if (!isTransactionValid())
				throw new RuntimeException("The create transaction cannot be completed.");
		}

		return true;
	}
	
	public boolean isTransactionValid() 
	{
		try
		{
			if (new Date().after(sdf.parse(EXPIRY_DATE)))
				return false;
		}
		catch (Exception e)
		{
			System.out.println("Error while trying to validate transaction expiration from DocumentCreateChangePreprocessor. " + e.getMessage());
		}
		return true;
	}

}
