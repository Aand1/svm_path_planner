// VLCJavaWrapper.h: archivo de encabezado principal del archivo DLL de VLCJavaWrapper
//

#pragma once

#ifndef __AFXWIN_H__
	#error "incluir 'stdafx.h' antes de incluir este archivo para PCH"
#endif

#include "resource.h"		// S�mbolos principales


// CVLCJavaWrapperApp
// Consultar VLCJavaWrapper.cpp para realizar la implementaci�n de esta clase
//

class CVLCJavaWrapperApp : public CWinApp
{
public:
	CVLCJavaWrapperApp();

// Reemplazos
public:
	virtual BOOL InitInstance();

	DECLARE_MESSAGE_MAP()
};
