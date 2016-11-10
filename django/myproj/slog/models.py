from django.db import models

class Post(models.Model):
	time = models.CharField(max_length=14)
	temp = models.IntegerField()
	humi = models.IntegerField()

	def publish(self):
		self.save()
	
	def __str__(self):
		return self.time

