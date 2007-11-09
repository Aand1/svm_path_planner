function [ imfilt ] = filtSelective( img, wsize )
%FILTSELECTIVE Summary of this function goes here
%   Detailed explanation goes here
%   Filtrado selectivo de la media, calcula la media en cuatro direcciones
% y se asigna como valor de la imagen filtrada la media m�s parecida al 
% pixel original.
%   img --> Imagen a filtrar
%   wsize --> Tama�o de la ventana del filtro

    imgsize = size(img);
    imfilt = zeros(imgsize);
    semiwsize = round(wsize/2);
    
    for i = semiwsize(1)+1:imgsize(1)-semiwsize(1)
        for j = semiwsize(2)+1:imgsize(2)-semiwsize(2)
            ver = mean(img(i+semiwsize(1):i+semiwsize(1), j)); % Media vertical
            hor = mean(img(i, j+semiwsize(2):j+semiwsize(2))); % Media horizontal            
    
            aux1 = img(i,j);                                    % Medias diagonales
            aux2 = img(i,j);
            for k = 1:semiwsize
                aux1 = [aux1, img(i-k, j-k), img(i+k, j+k)];
                aux2 = [aux2, img(i-k, j+k), img(i+k, j-k)];
            end
            diag1 = mean(aux1);
            diag2 = mean(aux2);
            
            means = [ver, hor, diag1, diag2];                   % Calcular el m�s parecido al central
            [minimum, index] = min(abs(uint8(means) - img(i,j)));
            
            imfilt(i,j) = means(index);
        end
    end
   