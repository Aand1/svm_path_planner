// stdafx.h: archivo de inclusi�n de los archivos de inclusi�n est�ndar del sistema
// o archivos de inclusi�n espec�ficos de un proyecto utilizados frecuentemente,
// pero rara vez modificados

#pragma once

#ifndef VC_EXTRALEAN
#define VC_EXTRALEAN		// Excluir material rara vez utilizado de encabezados de Windows
#endif

// Modifique las siguientes definiciones si tiene que seleccionar como destino una plataforma antes que las especificadas a continuaci�n.
// Consulte la referencia MSDN para obtener la informaci�n m�s reciente sobre los valores correspondientes a las diferentes plataformas.
#ifndef WINVER				// Permitir el uso de caracter�sticas espec�ficas de Windows XP o posterior.
#define WINVER 0x0501		// Cambiar al valor apropiado correspondiente a otras versiones de Windows.
#endif

#ifndef _WIN32_WINNT		// Permitir el uso de caracter�sticas espec�ficas de Windows XP o posterior.                   
#define _WIN32_WINNT 0x0501	// Cambiar al valor apropiado correspondiente a otras versiones de Windows.
#endif						

#ifndef _WIN32_WINDOWS		// Permitir el uso de caracter�sticas espec�ficas de Windows 98 o posterior.
#define _WIN32_WINDOWS 0x0410 // Cambiar a fin de establecer el valor apropiado para Windows Me o posterior.
#endif

#ifndef _WIN32_IE			// Permitir el uso de las caracter�sticas espec�ficas de IE 6.0 o posterior.
#define _WIN32_IE 0x0600	// Cambiar para establecer el valor apropiado a otras versiones de IE.
#endif

#define _ATL_CSTRING_EXPLICIT_CONSTRUCTORS	// Algunos constructores CString ser�n expl�citos

#include <afxwin.h>         // Componentes principales y est�ndar de MFC
#include <afxext.h>         // Extensiones de MFC

#ifndef _AFX_NO_OLE_SUPPORT
#include <afxole.h>         // Clases OLE de MFC
#include <afxodlgs.h>       // Clases de cuadros de di�logo OLE de MFC
#include <afxdisp.h>        // Clases de automatizaci�n de MFC
#endif // _AFX_NO_OLE_SUPPORT

#ifndef _AFX_NO_DB_SUPPORT
#include <afxdb.h>			// Clases de bases de datos ODBC MFC
#endif // _AFX_NO_DB_SUPPORT

#ifndef _AFX_NO_DAO_SUPPORT
#include <afxdao.h>			// Clases de bases de datos DAO MFC
#endif // _AFX_NO_DAO_SUPPORT

#ifndef _AFX_NO_OLE_SUPPORT
#include <afxdtctl.h>		// Compatibilidad MFC para controles comunes de Internet Explorer 4
#endif
#ifndef _AFX_NO_AFXCMN_SUPPORT
#include <afxcmn.h>			// Compatibilidad MFC para controles comunes de Windows
#endif // _AFX_NO_AFXCMN_SUPPORT

