from django.shortcuts import render
from django.http import HttpResponse
from .models import Post

# Create your views here.
def post_list(request):
	posts = Post.objects.order_by('time');
	return render(request, 'slog/post_list.html', {'posts':posts})

def register(request):
	print("12345\n")
	return HttpResponse("Hello") 

