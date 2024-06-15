# views.py

from django.shortcuts import render
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
import pandas as pd
import io
from .models import UploadedFile

@csrf_exempt
def upload_file(request):
    if request.method == 'POST' and request.FILES.get('upload'):
        file = request.FILES['upload']
        file_id = request.POST.get('file_id')  # Assuming file_id is sent in POST data

        if UploadedFile.objects.filter(file_id=file_id).exists():
            return JsonResponse({'message': 'File with this ID already exists. Do you want to override it?', 'exists': True}, status=200)

        try:
            # Process and save the file
            if file.name.endswith('.csv'):
                data = pd.read_csv(io.StringIO(file.read().decode('utf-8')))
                print(data)
                # Save file to database
                uploaded_file = UploadedFile(file_id=file_id, file=file)
                uploaded_file.save()
                return JsonResponse({'message': 'File uploaded successfully!', 'success': True})
            else:
                return JsonResponse({'message': 'Uploaded file is not a CSV.', 'success': False}, status=400)
        except Exception as e:
            return JsonResponse({'message': f'An error occurred: {str(e)}', 'success': False}, status=500)
    return render(request, 'upload.html')

@csrf_exempt
def override_file(request):
    if request.method == 'POST' and request.FILES.get('upload'):
        file = request.FILES['upload']
        file_id = request.POST.get('file_id')  # Assuming file_id is sent in POST data

        try:
            # Process and save the file
            if file.name.endswith('.csv'):
                data = pd.read_csv(io.StringIO(file.read().decode('utf-8')))
                print(data)
                # Override existing file in the database
                uploaded_file = UploadedFile.objects.get(file_id=file_id)
                uploaded_file.file = file
                uploaded_file.save()
                return JsonResponse({'message': 'File overridden successfully!', 'success': True})
            else:
                return JsonResponse({'message': 'Uploaded file is not a CSV.', 'success': False}, status=400)
        except Exception as e:
            return JsonResponse({'message': f'An error occurred: {str(e)}', 'success': False}, status=500)
    return render(request, 'upload.html')
