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
	public final static String EXPIRY_DATE = "2015-10-01"; // ProceSAR (Produccion)
	//public final static String EXPIRY_DATE = "2014-10-04"; // ACE Group (Desarrollo)
	
	public boolean preprocessObjectChange(IndependentlyPersistableObject sourceObj) throws EngineRuntimeException 
	{
		boolean isCreate = false;
		
		try 
		{
			// Check for various pending actions, could be helpful in determining what handler should do...
			PendingAction[] actions = sourceObj.getPendingActions();
			if ( actions != null )
			{
				for ( int i = 0; i < actions.length; i++ )
				{
					if (!isCreate)
						isCreate = actions[i] instanceof Create;
				}
			}
		} 
		catch (Exception e) 
		{
			System.out.println("Ocurrio un error al obtener las acciones pendientes en DocumentCreateChangePreprocessor. " + e.toString());
			return true;
		}
			
		if (isCreate) 
		{
			if (!isTransactionValid(EXPIRY_DATE))
				throw new RuntimeException("The create transaction cannot be completed.");
		}

		return true;
	}
	
	public boolean isTransactionValid(String expiryDateVal) 
	{
		try
		{
			Date expiryDate = sdf.parse(expiryDateVal);
			Date currentDate = new Date();
			if (currentDate.after(expiryDate))
				return false;
		}
		catch (Exception e)
		{
			System.out.println("Ocurrio un error al intentar validar la fecha de expiracion de la transaccion. " + e.toString());
		}
		return true;
	}

}
